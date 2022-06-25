package reactor.core.publisher;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Metrics;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;
import reactor.util.context.ContextView;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuple5;
import reactor.util.function.Tuple6;
import reactor.util.function.Tuple7;
import reactor.util.function.Tuple8;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

public abstract class Flux<T> implements CorePublisher<T> {
   static final BiFunction TUPLE2_BIFUNCTION = Tuples::of;
   static final Supplier LIST_SUPPLIER = ArrayList::new;
   static final Supplier SET_SUPPLIER = HashSet::new;
   static final BooleanSupplier ALWAYS_BOOLEAN_SUPPLIER = () -> true;
   static final BiPredicate OBJECT_EQUAL = Object::equals;
   static final Function IDENTITY_FUNCTION = Function.identity();

   @SafeVarargs
   public static <T, V> Flux<V> combineLatest(Function<Object[], V> combinator, Publisher<? extends T>... sources) {
      return combineLatest(combinator, Queues.XS_BUFFER_SIZE, sources);
   }

   @SafeVarargs
   public static <T, V> Flux<V> combineLatest(Function<Object[], V> combinator, int prefetch, Publisher<? extends T>... sources) {
      if (sources.length == 0) {
         return empty();
      } else if (sources.length == 1) {
         Publisher<? extends T> source = sources[0];
         return source instanceof Fuseable
            ? onAssembly(new FluxMapFuseable<>(from(source), v -> combinator.apply(new Object[]{v})))
            : onAssembly(new FluxMap<>(from(source), v -> combinator.apply(new Object[]{v})));
      } else {
         return onAssembly(new FluxCombineLatest<>(sources, combinator, Queues.get(prefetch), prefetch));
      }
   }

   public static <T1, T2, V> Flux<V> combineLatest(
      Publisher<? extends T1> source1, Publisher<? extends T2> source2, BiFunction<? super T1, ? super T2, ? extends V> combinator
   ) {
      return combineLatest(tuple -> combinator.apply(tuple[0], tuple[1]), source1, source2);
   }

   public static <T1, T2, T3, V> Flux<V> combineLatest(
      Publisher<? extends T1> source1, Publisher<? extends T2> source2, Publisher<? extends T3> source3, Function<Object[], V> combinator
   ) {
      return combineLatest(combinator, source1, source2, source3);
   }

   public static <T1, T2, T3, T4, V> Flux<V> combineLatest(
      Publisher<? extends T1> source1,
      Publisher<? extends T2> source2,
      Publisher<? extends T3> source3,
      Publisher<? extends T4> source4,
      Function<Object[], V> combinator
   ) {
      return combineLatest(combinator, source1, source2, source3, source4);
   }

   public static <T1, T2, T3, T4, T5, V> Flux<V> combineLatest(
      Publisher<? extends T1> source1,
      Publisher<? extends T2> source2,
      Publisher<? extends T3> source3,
      Publisher<? extends T4> source4,
      Publisher<? extends T5> source5,
      Function<Object[], V> combinator
   ) {
      return combineLatest(combinator, source1, source2, source3, source4, source5);
   }

   public static <T1, T2, T3, T4, T5, T6, V> Flux<V> combineLatest(
      Publisher<? extends T1> source1,
      Publisher<? extends T2> source2,
      Publisher<? extends T3> source3,
      Publisher<? extends T4> source4,
      Publisher<? extends T5> source5,
      Publisher<? extends T6> source6,
      Function<Object[], V> combinator
   ) {
      return combineLatest(combinator, source1, source2, source3, source4, source5, source6);
   }

   public static <T, V> Flux<V> combineLatest(Iterable<? extends Publisher<? extends T>> sources, Function<Object[], V> combinator) {
      return combineLatest(sources, Queues.XS_BUFFER_SIZE, combinator);
   }

   public static <T, V> Flux<V> combineLatest(Iterable<? extends Publisher<? extends T>> sources, int prefetch, Function<Object[], V> combinator) {
      return onAssembly(new FluxCombineLatest<>(sources, combinator, Queues.get(prefetch), prefetch));
   }

   public static <T> Flux<T> concat(Iterable<? extends Publisher<? extends T>> sources) {
      return onAssembly(new FluxConcatIterable<>(sources));
   }

   @SafeVarargs
   public final Flux<T> concatWithValues(T... values) {
      return this.concatWith(fromArray(values));
   }

   public static <T> Flux<T> concat(Publisher<? extends Publisher<? extends T>> sources) {
      return concat(sources, Queues.XS_BUFFER_SIZE);
   }

   public static <T> Flux<T> concat(Publisher<? extends Publisher<? extends T>> sources, int prefetch) {
      return from(sources).concatMap(identityFunction(), prefetch);
   }

   @SafeVarargs
   public static <T> Flux<T> concat(Publisher<? extends T>... sources) {
      return onAssembly(new FluxConcatArray<>(false, sources));
   }

   public static <T> Flux<T> concatDelayError(Publisher<? extends Publisher<? extends T>> sources) {
      return concatDelayError(sources, Queues.XS_BUFFER_SIZE);
   }

   public static <T> Flux<T> concatDelayError(Publisher<? extends Publisher<? extends T>> sources, int prefetch) {
      return from(sources).concatMapDelayError(identityFunction(), prefetch);
   }

   public static <T> Flux<T> concatDelayError(Publisher<? extends Publisher<? extends T>> sources, boolean delayUntilEnd, int prefetch) {
      return from(sources).concatMapDelayError(identityFunction(), delayUntilEnd, prefetch);
   }

   @SafeVarargs
   public static <T> Flux<T> concatDelayError(Publisher<? extends T>... sources) {
      return onAssembly(new FluxConcatArray<>(true, sources));
   }

   public static <T> Flux<T> create(Consumer<? super FluxSink<T>> emitter) {
      return create(emitter, FluxSink.OverflowStrategy.BUFFER);
   }

   public static <T> Flux<T> create(Consumer<? super FluxSink<T>> emitter, FluxSink.OverflowStrategy backpressure) {
      return onAssembly(new FluxCreate<>(emitter, backpressure, FluxCreate.CreateMode.PUSH_PULL));
   }

   public static <T> Flux<T> push(Consumer<? super FluxSink<T>> emitter) {
      return push(emitter, FluxSink.OverflowStrategy.BUFFER);
   }

   public static <T> Flux<T> push(Consumer<? super FluxSink<T>> emitter, FluxSink.OverflowStrategy backpressure) {
      return onAssembly(new FluxCreate<>(emitter, backpressure, FluxCreate.CreateMode.PUSH_ONLY));
   }

   public static <T> Flux<T> defer(Supplier<? extends Publisher<T>> supplier) {
      return onAssembly(new FluxDefer<>(supplier));
   }

   @Deprecated
   public static <T> Flux<T> deferWithContext(Function<Context, ? extends Publisher<T>> contextualPublisherFactory) {
      return deferContextual(view -> (Publisher)contextualPublisherFactory.apply(Context.of(view)));
   }

   public static <T> Flux<T> deferContextual(Function<ContextView, ? extends Publisher<T>> contextualPublisherFactory) {
      return onAssembly(new FluxDeferContextual<>(contextualPublisherFactory));
   }

   public static <T> Flux<T> empty() {
      return FluxEmpty.instance();
   }

   public static <T> Flux<T> error(Throwable error) {
      return error(error, false);
   }

   public static <T> Flux<T> error(Supplier<? extends Throwable> errorSupplier) {
      return onAssembly(new FluxErrorSupplied<>(errorSupplier));
   }

   public static <O> Flux<O> error(Throwable throwable, boolean whenRequested) {
      return whenRequested ? onAssembly(new FluxErrorOnRequest<>(throwable)) : onAssembly(new FluxError<>(throwable));
   }

   @SafeVarargs
   @Deprecated
   public static <I> Flux<I> first(Publisher<? extends I>... sources) {
      return firstWithSignal(sources);
   }

   @Deprecated
   public static <I> Flux<I> first(Iterable<? extends Publisher<? extends I>> sources) {
      return firstWithSignal(sources);
   }

   @SafeVarargs
   public static <I> Flux<I> firstWithSignal(Publisher<? extends I>... sources) {
      return onAssembly(new FluxFirstWithSignal<>(sources));
   }

   public static <I> Flux<I> firstWithSignal(Iterable<? extends Publisher<? extends I>> sources) {
      return onAssembly(new FluxFirstWithSignal<>(sources));
   }

   public static <I> Flux<I> firstWithValue(Iterable<? extends Publisher<? extends I>> sources) {
      return onAssembly(new FluxFirstWithValue<>(sources));
   }

   @SafeVarargs
   public static <I> Flux<I> firstWithValue(Publisher<? extends I> first, Publisher<? extends I>... others) {
      if (first instanceof FluxFirstWithValue) {
         FluxFirstWithValue<I> orPublisher = (FluxFirstWithValue)first;
         FluxFirstWithValue<I> result = orPublisher.firstValuedAdditionalSources(others);
         if (result != null) {
            return result;
         }
      }

      return onAssembly(new FluxFirstWithValue<>(first, others));
   }

   public static <T> Flux<T> from(Publisher<? extends T> source) {
      return source instanceof Flux ? (Flux)source : onAssembly(wrap(source));
   }

   public static <T> Flux<T> fromArray(T[] array) {
      if (array.length == 0) {
         return empty();
      } else {
         return array.length == 1 ? just(array[0]) : onAssembly(new FluxArray<>(array));
      }
   }

   public static <T> Flux<T> fromIterable(Iterable<? extends T> it) {
      return onAssembly(new FluxIterable<>(it));
   }

   public static <T> Flux<T> fromStream(Stream<? extends T> s) {
      Objects.requireNonNull(s, "Stream s must be provided");
      return onAssembly(new FluxStream<>(() -> s));
   }

   public static <T> Flux<T> fromStream(Supplier<Stream<? extends T>> streamSupplier) {
      return onAssembly(new FluxStream<>(streamSupplier));
   }

   public static <T> Flux<T> generate(Consumer<SynchronousSink<T>> generator) {
      Objects.requireNonNull(generator, "generator");
      return onAssembly(new FluxGenerate<>(generator));
   }

   public static <T, S> Flux<T> generate(Callable<S> stateSupplier, BiFunction<S, SynchronousSink<T>, S> generator) {
      return onAssembly(new FluxGenerate<>(stateSupplier, generator));
   }

   public static <T, S> Flux<T> generate(Callable<S> stateSupplier, BiFunction<S, SynchronousSink<T>, S> generator, Consumer<? super S> stateConsumer) {
      return onAssembly(new FluxGenerate<>(stateSupplier, generator, stateConsumer));
   }

   public static Flux<Long> interval(Duration period) {
      return interval(period, Schedulers.parallel());
   }

   public static Flux<Long> interval(Duration delay, Duration period) {
      return interval(delay, period, Schedulers.parallel());
   }

   public static Flux<Long> interval(Duration period, Scheduler timer) {
      return interval(period, period, timer);
   }

   public static Flux<Long> interval(Duration delay, Duration period, Scheduler timer) {
      return onAssembly(new FluxInterval(delay.toNanos(), period.toNanos(), TimeUnit.NANOSECONDS, timer));
   }

   @SafeVarargs
   public static <T> Flux<T> just(T... data) {
      return fromArray(data);
   }

   public static <T> Flux<T> just(T data) {
      return onAssembly(new FluxJust<>(data));
   }

   public static <T> Flux<T> merge(Publisher<? extends Publisher<? extends T>> source) {
      return merge(source, Queues.SMALL_BUFFER_SIZE, Queues.XS_BUFFER_SIZE);
   }

   public static <T> Flux<T> merge(Publisher<? extends Publisher<? extends T>> source, int concurrency) {
      return merge(source, concurrency, Queues.XS_BUFFER_SIZE);
   }

   public static <T> Flux<T> merge(Publisher<? extends Publisher<? extends T>> source, int concurrency, int prefetch) {
      return onAssembly(new FluxFlatMap<>(from(source), identityFunction(), false, concurrency, Queues.get(concurrency), prefetch, Queues.get(prefetch)));
   }

   public static <I> Flux<I> merge(Iterable<? extends Publisher<? extends I>> sources) {
      return merge(fromIterable(sources));
   }

   @SafeVarargs
   public static <I> Flux<I> merge(Publisher<? extends I>... sources) {
      return merge(Queues.XS_BUFFER_SIZE, sources);
   }

   @SafeVarargs
   public static <I> Flux<I> merge(int prefetch, Publisher<? extends I>... sources) {
      return merge(prefetch, false, sources);
   }

   @SafeVarargs
   public static <I> Flux<I> mergeDelayError(int prefetch, Publisher<? extends I>... sources) {
      return merge(prefetch, true, sources);
   }

   @SafeVarargs
   public static <I extends Comparable<? super I>> Flux<I> mergeComparing(Publisher<? extends I>... sources) {
      return mergeComparing(Queues.SMALL_BUFFER_SIZE, Comparator.naturalOrder(), sources);
   }

   @SafeVarargs
   public static <T> Flux<T> mergeComparing(Comparator<? super T> comparator, Publisher<? extends T>... sources) {
      return mergeComparing(Queues.SMALL_BUFFER_SIZE, comparator, sources);
   }

   @SafeVarargs
   public static <T> Flux<T> mergeComparing(int prefetch, Comparator<? super T> comparator, Publisher<? extends T>... sources) {
      if (sources.length == 0) {
         return empty();
      } else {
         return sources.length == 1 ? from(sources[0]) : onAssembly(new FluxMergeComparing<>(prefetch, comparator, false, sources));
      }
   }

   @SafeVarargs
   public static <T> Flux<T> mergeComparingDelayError(int prefetch, Comparator<? super T> comparator, Publisher<? extends T>... sources) {
      if (sources.length == 0) {
         return empty();
      } else {
         return sources.length == 1 ? from(sources[0]) : onAssembly(new FluxMergeComparing<>(prefetch, comparator, true, sources));
      }
   }

   @SafeVarargs
   @Deprecated
   public static <I extends Comparable<? super I>> Flux<I> mergeOrdered(Publisher<? extends I>... sources) {
      return mergeOrdered(Queues.SMALL_BUFFER_SIZE, Comparator.naturalOrder(), sources);
   }

   @SafeVarargs
   @Deprecated
   public static <T> Flux<T> mergeOrdered(Comparator<? super T> comparator, Publisher<? extends T>... sources) {
      return mergeOrdered(Queues.SMALL_BUFFER_SIZE, comparator, sources);
   }

   @SafeVarargs
   @Deprecated
   public static <T> Flux<T> mergeOrdered(int prefetch, Comparator<? super T> comparator, Publisher<? extends T>... sources) {
      if (sources.length == 0) {
         return empty();
      } else {
         return sources.length == 1 ? from(sources[0]) : onAssembly(new FluxMergeComparing<>(prefetch, comparator, true, sources));
      }
   }

   public static <T> Flux<T> mergeSequential(Publisher<? extends Publisher<? extends T>> sources) {
      return mergeSequential(sources, false, Queues.SMALL_BUFFER_SIZE, Queues.XS_BUFFER_SIZE);
   }

   public static <T> Flux<T> mergeSequential(Publisher<? extends Publisher<? extends T>> sources, int maxConcurrency, int prefetch) {
      return mergeSequential(sources, false, maxConcurrency, prefetch);
   }

   public static <T> Flux<T> mergeSequentialDelayError(Publisher<? extends Publisher<? extends T>> sources, int maxConcurrency, int prefetch) {
      return mergeSequential(sources, true, maxConcurrency, prefetch);
   }

   @SafeVarargs
   public static <I> Flux<I> mergeSequential(Publisher<? extends I>... sources) {
      return mergeSequential(Queues.XS_BUFFER_SIZE, false, sources);
   }

   @SafeVarargs
   public static <I> Flux<I> mergeSequential(int prefetch, Publisher<? extends I>... sources) {
      return mergeSequential(prefetch, false, sources);
   }

   @SafeVarargs
   public static <I> Flux<I> mergeSequentialDelayError(int prefetch, Publisher<? extends I>... sources) {
      return mergeSequential(prefetch, true, sources);
   }

   public static <I> Flux<I> mergeSequential(Iterable<? extends Publisher<? extends I>> sources) {
      return mergeSequential(sources, false, Queues.SMALL_BUFFER_SIZE, Queues.XS_BUFFER_SIZE);
   }

   public static <I> Flux<I> mergeSequential(Iterable<? extends Publisher<? extends I>> sources, int maxConcurrency, int prefetch) {
      return mergeSequential(sources, false, maxConcurrency, prefetch);
   }

   public static <I> Flux<I> mergeSequentialDelayError(Iterable<? extends Publisher<? extends I>> sources, int maxConcurrency, int prefetch) {
      return mergeSequential(sources, true, maxConcurrency, prefetch);
   }

   public static <T> Flux<T> never() {
      return FluxNever.instance();
   }

   public static Flux<Integer> range(int start, int count) {
      if (count == 1) {
         return just((T)start);
      } else {
         return count == 0 ? empty() : onAssembly(new FluxRange(start, count));
      }
   }

   public static <T> Flux<T> switchOnNext(Publisher<? extends Publisher<? extends T>> mergedPublishers) {
      return switchOnNext(mergedPublishers, Queues.XS_BUFFER_SIZE);
   }

   @Deprecated
   public static <T> Flux<T> switchOnNext(Publisher<? extends Publisher<? extends T>> mergedPublishers, int prefetch) {
      return prefetch == 0
         ? onAssembly(new FluxSwitchMapNoPrefetch<>(from(mergedPublishers), identityFunction()))
         : onAssembly(new FluxSwitchMap<>(from(mergedPublishers), identityFunction(), Queues.unbounded(prefetch), prefetch));
   }

   public static <T, D> Flux<T> using(
      Callable<? extends D> resourceSupplier, Function<? super D, ? extends Publisher<? extends T>> sourceSupplier, Consumer<? super D> resourceCleanup
   ) {
      return using(resourceSupplier, sourceSupplier, resourceCleanup, true);
   }

   public static <T, D> Flux<T> using(
      Callable<? extends D> resourceSupplier,
      Function<? super D, ? extends Publisher<? extends T>> sourceSupplier,
      Consumer<? super D> resourceCleanup,
      boolean eager
   ) {
      return onAssembly(new FluxUsing<>(resourceSupplier, sourceSupplier, resourceCleanup, eager));
   }

   public static <T, D> Flux<T> usingWhen(
      Publisher<D> resourceSupplier,
      Function<? super D, ? extends Publisher<? extends T>> resourceClosure,
      Function<? super D, ? extends Publisher<?>> asyncCleanup
   ) {
      return usingWhen(resourceSupplier, resourceClosure, asyncCleanup, (resource, error) -> (Publisher)asyncCleanup.apply(resource), asyncCleanup);
   }

   public static <T, D> Flux<T> usingWhen(
      Publisher<D> resourceSupplier,
      Function<? super D, ? extends Publisher<? extends T>> resourceClosure,
      Function<? super D, ? extends Publisher<?>> asyncComplete,
      BiFunction<? super D, ? super Throwable, ? extends Publisher<?>> asyncError,
      Function<? super D, ? extends Publisher<?>> asyncCancel
   ) {
      return onAssembly(new FluxUsingWhen<>(resourceSupplier, resourceClosure, asyncComplete, asyncError, asyncCancel));
   }

   public static <T1, T2, O> Flux<O> zip(
      Publisher<? extends T1> source1, Publisher<? extends T2> source2, BiFunction<? super T1, ? super T2, ? extends O> combinator
   ) {
      return onAssembly(new FluxZip<>(source1, source2, combinator, Queues.xs(), Queues.XS_BUFFER_SIZE));
   }

   public static <T1, T2> Flux<Tuple2<T1, T2>> zip(Publisher<? extends T1> source1, Publisher<? extends T2> source2) {
      return zip(source1, source2, tuple2Function());
   }

   public static <T1, T2, T3> Flux<Tuple3<T1, T2, T3>> zip(Publisher<? extends T1> source1, Publisher<? extends T2> source2, Publisher<? extends T3> source3) {
      return zip(Tuples.fn3(), source1, source2, source3);
   }

   public static <T1, T2, T3, T4> Flux<Tuple4<T1, T2, T3, T4>> zip(
      Publisher<? extends T1> source1, Publisher<? extends T2> source2, Publisher<? extends T3> source3, Publisher<? extends T4> source4
   ) {
      return zip(Tuples.fn4(), source1, source2, source3, source4);
   }

   public static <T1, T2, T3, T4, T5> Flux<Tuple5<T1, T2, T3, T4, T5>> zip(
      Publisher<? extends T1> source1,
      Publisher<? extends T2> source2,
      Publisher<? extends T3> source3,
      Publisher<? extends T4> source4,
      Publisher<? extends T5> source5
   ) {
      return zip(Tuples.fn5(), source1, source2, source3, source4, source5);
   }

   public static <T1, T2, T3, T4, T5, T6> Flux<Tuple6<T1, T2, T3, T4, T5, T6>> zip(
      Publisher<? extends T1> source1,
      Publisher<? extends T2> source2,
      Publisher<? extends T3> source3,
      Publisher<? extends T4> source4,
      Publisher<? extends T5> source5,
      Publisher<? extends T6> source6
   ) {
      return zip(Tuples.fn6(), source1, source2, source3, source4, source5, source6);
   }

   public static <T1, T2, T3, T4, T5, T6, T7> Flux<Tuple7<T1, T2, T3, T4, T5, T6, T7>> zip(
      Publisher<? extends T1> source1,
      Publisher<? extends T2> source2,
      Publisher<? extends T3> source3,
      Publisher<? extends T4> source4,
      Publisher<? extends T5> source5,
      Publisher<? extends T6> source6,
      Publisher<? extends T7> source7
   ) {
      return zip(Tuples.fn7(), source1, source2, source3, source4, source5, source6, source7);
   }

   public static <T1, T2, T3, T4, T5, T6, T7, T8> Flux<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> zip(
      Publisher<? extends T1> source1,
      Publisher<? extends T2> source2,
      Publisher<? extends T3> source3,
      Publisher<? extends T4> source4,
      Publisher<? extends T5> source5,
      Publisher<? extends T6> source6,
      Publisher<? extends T7> source7,
      Publisher<? extends T8> source8
   ) {
      return zip(Tuples.fn8(), source1, source2, source3, source4, source5, source6, source7, source8);
   }

   public static <O> Flux<O> zip(Iterable<? extends Publisher<?>> sources, Function<? super Object[], ? extends O> combinator) {
      return zip(sources, Queues.XS_BUFFER_SIZE, combinator);
   }

   public static <O> Flux<O> zip(Iterable<? extends Publisher<?>> sources, int prefetch, Function<? super Object[], ? extends O> combinator) {
      return onAssembly(new FluxZip<>(sources, combinator, Queues.get(prefetch), prefetch));
   }

   @SafeVarargs
   public static <I, O> Flux<O> zip(Function<? super Object[], ? extends O> combinator, Publisher<? extends I>... sources) {
      return zip(combinator, Queues.XS_BUFFER_SIZE, sources);
   }

   @SafeVarargs
   public static <I, O> Flux<O> zip(Function<? super Object[], ? extends O> combinator, int prefetch, Publisher<? extends I>... sources) {
      if (sources.length == 0) {
         return empty();
      } else if (sources.length == 1) {
         Publisher<? extends I> source = sources[0];
         return source instanceof Fuseable
            ? onAssembly(new FluxMapFuseable<>(from(source), v -> combinator.apply(new Object[]{v})))
            : onAssembly(new FluxMap<>(from(source), v -> combinator.apply(new Object[]{v})));
      } else {
         return onAssembly(new FluxZip<>(sources, combinator, Queues.get(prefetch), prefetch));
      }
   }

   public static <TUPLE extends Tuple2, V> Flux<V> zip(Publisher<? extends Publisher<?>> sources, final Function<? super TUPLE, ? extends V> combinator) {
      return onAssembly(new FluxBuffer(from(sources), Integer.MAX_VALUE, listSupplier()).flatMap(new Function<List<? extends Publisher<?>>, Publisher<V>>() {
         public Publisher<V> apply(List<? extends Publisher<?>> publishers) {
            return Flux.zip(Tuples.fnAny(combinator), (Publisher[])publishers.toArray(new Publisher[publishers.size()]));
         }
      }));
   }

   public final Mono<Boolean> all(Predicate<? super T> predicate) {
      return Mono.onAssembly(new MonoAll<>(this, predicate));
   }

   public final Mono<Boolean> any(Predicate<? super T> predicate) {
      return Mono.onAssembly(new MonoAny<>(this, predicate));
   }

   public final <P> P as(Function<? super Flux<T>, P> transformer) {
      return (P)transformer.apply(this);
   }

   @Nullable
   public final T blockFirst() {
      BlockingFirstSubscriber<T> subscriber = new BlockingFirstSubscriber<>();
      this.subscribe(subscriber);
      return subscriber.blockingGet();
   }

   @Nullable
   public final T blockFirst(Duration timeout) {
      BlockingFirstSubscriber<T> subscriber = new BlockingFirstSubscriber<>();
      this.subscribe(subscriber);
      return subscriber.blockingGet(timeout.toNanos(), TimeUnit.NANOSECONDS);
   }

   @Nullable
   public final T blockLast() {
      BlockingLastSubscriber<T> subscriber = new BlockingLastSubscriber<>();
      this.subscribe(subscriber);
      return subscriber.blockingGet();
   }

   @Nullable
   public final T blockLast(Duration timeout) {
      BlockingLastSubscriber<T> subscriber = new BlockingLastSubscriber<>();
      this.subscribe(subscriber);
      return subscriber.blockingGet(timeout.toNanos(), TimeUnit.NANOSECONDS);
   }

   public final Flux<List<T>> buffer() {
      return this.buffer(Integer.MAX_VALUE);
   }

   public final Flux<List<T>> buffer(int maxSize) {
      return this.buffer(maxSize, listSupplier());
   }

   public final <C extends Collection<? super T>> Flux<C> buffer(int maxSize, Supplier<C> bufferSupplier) {
      return onAssembly(new FluxBuffer<>(this, maxSize, bufferSupplier));
   }

   public final Flux<List<T>> buffer(int maxSize, int skip) {
      return this.buffer(maxSize, skip, listSupplier());
   }

   public final <C extends Collection<? super T>> Flux<C> buffer(int maxSize, int skip, Supplier<C> bufferSupplier) {
      return onAssembly(new FluxBuffer<>(this, maxSize, skip, bufferSupplier));
   }

   public final Flux<List<T>> buffer(Publisher<?> other) {
      return this.buffer(other, listSupplier());
   }

   public final <C extends Collection<? super T>> Flux<C> buffer(Publisher<?> other, Supplier<C> bufferSupplier) {
      return onAssembly(new FluxBufferBoundary<>(this, other, bufferSupplier));
   }

   public final Flux<List<T>> buffer(Duration bufferingTimespan) {
      return this.buffer(bufferingTimespan, Schedulers.parallel());
   }

   public final Flux<List<T>> buffer(Duration bufferingTimespan, Duration openBufferEvery) {
      return this.buffer(bufferingTimespan, openBufferEvery, Schedulers.parallel());
   }

   public final Flux<List<T>> buffer(Duration bufferingTimespan, Scheduler timer) {
      return this.buffer(interval(bufferingTimespan, timer));
   }

   public final Flux<List<T>> buffer(Duration bufferingTimespan, Duration openBufferEvery, Scheduler timer) {
      return bufferingTimespan.equals(openBufferEvery)
         ? this.buffer(bufferingTimespan, timer)
         : this.bufferWhen(interval(Duration.ZERO, openBufferEvery, timer), aLong -> Mono.delay(bufferingTimespan, timer));
   }

   public final Flux<List<T>> bufferTimeout(int maxSize, Duration maxTime) {
      return this.bufferTimeout(maxSize, maxTime, listSupplier());
   }

   public final <C extends Collection<? super T>> Flux<C> bufferTimeout(int maxSize, Duration maxTime, Supplier<C> bufferSupplier) {
      return this.bufferTimeout(maxSize, maxTime, Schedulers.parallel(), bufferSupplier);
   }

   public final Flux<List<T>> bufferTimeout(int maxSize, Duration maxTime, Scheduler timer) {
      return this.bufferTimeout(maxSize, maxTime, timer, listSupplier());
   }

   public final <C extends Collection<? super T>> Flux<C> bufferTimeout(int maxSize, Duration maxTime, Scheduler timer, Supplier<C> bufferSupplier) {
      return onAssembly(new FluxBufferTimeout<>(this, maxSize, maxTime.toNanos(), TimeUnit.NANOSECONDS, timer, bufferSupplier));
   }

   public final Flux<List<T>> bufferUntil(Predicate<? super T> predicate) {
      return onAssembly(new FluxBufferPredicate<>(this, predicate, listSupplier(), FluxBufferPredicate.Mode.UNTIL));
   }

   public final Flux<List<T>> bufferUntil(Predicate<? super T> predicate, boolean cutBefore) {
      return onAssembly(
         new FluxBufferPredicate<>(this, predicate, listSupplier(), cutBefore ? FluxBufferPredicate.Mode.UNTIL_CUT_BEFORE : FluxBufferPredicate.Mode.UNTIL)
      );
   }

   public final <V> Flux<List<T>> bufferUntilChanged() {
      return this.bufferUntilChanged(identityFunction());
   }

   public final <V> Flux<List<T>> bufferUntilChanged(Function<? super T, ? extends V> keySelector) {
      return this.bufferUntilChanged(keySelector, equalPredicate());
   }

   public final <V> Flux<List<T>> bufferUntilChanged(Function<? super T, ? extends V> keySelector, BiPredicate<? super V, ? super V> keyComparator) {
      return defer(() -> this.bufferUntil(new FluxBufferPredicate.ChangedPredicate(keySelector, keyComparator), true));
   }

   public final Flux<List<T>> bufferWhile(Predicate<? super T> predicate) {
      return onAssembly(new FluxBufferPredicate<>(this, predicate, listSupplier(), FluxBufferPredicate.Mode.WHILE));
   }

   public final <U, V> Flux<List<T>> bufferWhen(Publisher<U> bucketOpening, Function<? super U, ? extends Publisher<V>> closeSelector) {
      return this.bufferWhen(bucketOpening, closeSelector, listSupplier());
   }

   public final <U, V, C extends Collection<? super T>> Flux<C> bufferWhen(
      Publisher<U> bucketOpening, Function<? super U, ? extends Publisher<V>> closeSelector, Supplier<C> bufferSupplier
   ) {
      return onAssembly(new FluxBufferWhen<>(this, bucketOpening, closeSelector, bufferSupplier, Queues.unbounded(Queues.XS_BUFFER_SIZE)));
   }

   public final Flux<T> cache() {
      return this.cache(Integer.MAX_VALUE);
   }

   public final Flux<T> cache(int history) {
      return this.replay(history).autoConnect();
   }

   public final Flux<T> cache(Duration ttl) {
      return this.cache(ttl, Schedulers.parallel());
   }

   public final Flux<T> cache(Duration ttl, Scheduler timer) {
      return this.cache(Integer.MAX_VALUE, ttl, timer);
   }

   public final Flux<T> cache(int history, Duration ttl) {
      return this.cache(history, ttl, Schedulers.parallel());
   }

   public final Flux<T> cache(int history, Duration ttl, Scheduler timer) {
      return this.replay(history, ttl, timer).autoConnect();
   }

   public final <E> Flux<E> cast(Class<E> clazz) {
      Objects.requireNonNull(clazz, "clazz");
      return this.map(clazz::cast);
   }

   public final Flux<T> cancelOn(Scheduler scheduler) {
      return onAssembly(new FluxCancelOn<>(this, scheduler));
   }

   public final Flux<T> checkpoint() {
      return this.checkpoint(null, true);
   }

   public final Flux<T> checkpoint(String description) {
      return this.checkpoint((String)Objects.requireNonNull(description), false);
   }

   public final Flux<T> checkpoint(@Nullable String description, boolean forceStackTrace) {
      FluxOnAssembly.AssemblySnapshot stacktrace;
      if (!forceStackTrace) {
         stacktrace = new FluxOnAssembly.CheckpointLightSnapshot(description);
      } else {
         stacktrace = new FluxOnAssembly.CheckpointHeavySnapshot(description, (Supplier<String>)Traces.callSiteSupplierFactory.get());
      }

      return new FluxOnAssembly<>(this, stacktrace);
   }

   public final <E> Mono<E> collect(Supplier<E> containerSupplier, BiConsumer<E, ? super T> collector) {
      return Mono.onAssembly(new MonoCollect<>(this, containerSupplier, collector));
   }

   public final <R, A> Mono<R> collect(Collector<? super T, A, ? extends R> collector) {
      return Mono.onAssembly(new MonoStreamCollector<>(this, collector));
   }

   public final Mono<List<T>> collectList() {
      if (this instanceof Callable) {
         if (this instanceof Fuseable.ScalarCallable) {
            Fuseable.ScalarCallable<T> scalarCallable = (Fuseable.ScalarCallable)this;

            T v;
            try {
               v = (T)scalarCallable.call();
            } catch (Exception var4) {
               return Mono.error(Exceptions.unwrap(var4));
            }

            return Mono.onAssembly(new MonoCallable<>(() -> {
               List<T> list = (List)listSupplier().get();
               if (v != null) {
                  list.add(v);
               }

               return list;
            }));
         } else {
            Callable<T> thiz = (Callable)this;
            return Mono.onAssembly(new MonoCallable<>(() -> {
               List<T> list = (List)listSupplier().get();
               T u = (T)thiz.call();
               if (u != null) {
                  list.add(u);
               }

               return list;
            }));
         }
      } else {
         return Mono.onAssembly(new MonoCollectList<>(this));
      }
   }

   public final <K> Mono<Map<K, T>> collectMap(Function<? super T, ? extends K> keyExtractor) {
      return this.collectMap(keyExtractor, identityFunction());
   }

   public final <K, V> Mono<Map<K, V>> collectMap(Function<? super T, ? extends K> keyExtractor, Function<? super T, ? extends V> valueExtractor) {
      return this.collectMap(keyExtractor, valueExtractor, () -> new HashMap());
   }

   public final <K, V> Mono<Map<K, V>> collectMap(
      Function<? super T, ? extends K> keyExtractor, Function<? super T, ? extends V> valueExtractor, Supplier<Map<K, V>> mapSupplier
   ) {
      Objects.requireNonNull(keyExtractor, "Key extractor is null");
      Objects.requireNonNull(valueExtractor, "Value extractor is null");
      Objects.requireNonNull(mapSupplier, "Map supplier is null");
      return this.collect(mapSupplier, (m, d) -> m.put(keyExtractor.apply(d), valueExtractor.apply(d)));
   }

   public final <K> Mono<Map<K, Collection<T>>> collectMultimap(Function<? super T, ? extends K> keyExtractor) {
      return this.collectMultimap(keyExtractor, identityFunction());
   }

   public final <K, V> Mono<Map<K, Collection<V>>> collectMultimap(
      Function<? super T, ? extends K> keyExtractor, Function<? super T, ? extends V> valueExtractor
   ) {
      return this.collectMultimap(keyExtractor, valueExtractor, () -> new HashMap());
   }

   public final <K, V> Mono<Map<K, Collection<V>>> collectMultimap(
      Function<? super T, ? extends K> keyExtractor, Function<? super T, ? extends V> valueExtractor, Supplier<Map<K, Collection<V>>> mapSupplier
   ) {
      Objects.requireNonNull(keyExtractor, "Key extractor is null");
      Objects.requireNonNull(valueExtractor, "Value extractor is null");
      Objects.requireNonNull(mapSupplier, "Map supplier is null");
      return this.collect(mapSupplier, (m, d) -> {
         K key = (K)keyExtractor.apply(d);
         Collection<V> values = (Collection)m.computeIfAbsent(key, k -> new ArrayList());
         values.add(valueExtractor.apply(d));
      });
   }

   public final Mono<List<T>> collectSortedList() {
      return this.collectSortedList(null);
   }

   public final Mono<List<T>> collectSortedList(@Nullable Comparator<? super T> comparator) {
      return this.collectList().doOnNext(list -> list.sort(comparator));
   }

   public final <V> Flux<V> concatMap(Function<? super T, ? extends Publisher<? extends V>> mapper) {
      return this.concatMap(mapper, Queues.XS_BUFFER_SIZE);
   }

   public final <V> Flux<V> concatMap(Function<? super T, ? extends Publisher<? extends V>> mapper, int prefetch) {
      return prefetch == 0
         ? onAssembly(new FluxConcatMapNoPrefetch<>(this, mapper, FluxConcatMap.ErrorMode.IMMEDIATE))
         : onAssembly(new FluxConcatMap<>(this, mapper, Queues.get(prefetch), prefetch, FluxConcatMap.ErrorMode.IMMEDIATE));
   }

   public final <V> Flux<V> concatMapDelayError(Function<? super T, ? extends Publisher<? extends V>> mapper) {
      return this.concatMapDelayError(mapper, Queues.XS_BUFFER_SIZE);
   }

   public final <V> Flux<V> concatMapDelayError(Function<? super T, ? extends Publisher<? extends V>> mapper, int prefetch) {
      return this.concatMapDelayError(mapper, true, prefetch);
   }

   public final <V> Flux<V> concatMapDelayError(Function<? super T, ? extends Publisher<? extends V>> mapper, boolean delayUntilEnd, int prefetch) {
      FluxConcatMap.ErrorMode errorMode = delayUntilEnd ? FluxConcatMap.ErrorMode.END : FluxConcatMap.ErrorMode.BOUNDARY;
      return prefetch == 0
         ? onAssembly(new FluxConcatMapNoPrefetch<>(this, mapper, errorMode))
         : onAssembly(new FluxConcatMap<>(this, mapper, Queues.get(prefetch), prefetch, errorMode));
   }

   public final <R> Flux<R> concatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
      return this.concatMapIterable(mapper, Queues.XS_BUFFER_SIZE);
   }

   public final <R> Flux<R> concatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper, int prefetch) {
      return onAssembly(new FluxFlattenIterable<>(this, mapper, prefetch, Queues.get(prefetch)));
   }

   public final Flux<T> concatWith(Publisher<? extends T> other) {
      if (this instanceof FluxConcatArray) {
         FluxConcatArray<T> fluxConcatArray = (FluxConcatArray)this;
         return fluxConcatArray.concatAdditionalSourceLast(other);
      } else {
         return concat(this, other);
      }
   }

   public final Flux<T> contextWrite(ContextView contextToAppend) {
      return this.contextWrite(c -> c.putAll(contextToAppend));
   }

   public final Flux<T> contextWrite(Function<Context, Context> contextModifier) {
      return onAssembly(new FluxContextWrite<>(this, contextModifier));
   }

   public final Mono<Long> count() {
      return Mono.onAssembly(new MonoCount<>(this));
   }

   public final Flux<T> defaultIfEmpty(T defaultV) {
      return onAssembly(new FluxDefaultIfEmpty<>(this, defaultV));
   }

   public final Flux<T> delayElements(Duration delay) {
      return this.delayElements(delay, Schedulers.parallel());
   }

   public final Flux<T> delayElements(Duration delay, Scheduler timer) {
      return this.delayUntil(d -> Mono.delay(delay, timer));
   }

   public final Flux<T> delaySequence(Duration delay) {
      return this.delaySequence(delay, Schedulers.parallel());
   }

   public final Flux<T> delaySequence(Duration delay, Scheduler timer) {
      return onAssembly(new FluxDelaySequence<>(this, delay, timer));
   }

   public final Flux<T> delayUntil(Function<? super T, ? extends Publisher<?>> triggerProvider) {
      return this.concatMap(v -> Mono.just(v).delayUntil(triggerProvider));
   }

   public final Flux<T> delaySubscription(Duration delay) {
      return this.delaySubscription(delay, Schedulers.parallel());
   }

   public final Flux<T> delaySubscription(Duration delay, Scheduler timer) {
      return this.delaySubscription(Mono.delay(delay, timer));
   }

   public final <U> Flux<T> delaySubscription(Publisher<U> subscriptionDelay) {
      return onAssembly(new FluxDelaySubscription<>(this, subscriptionDelay));
   }

   public final <X> Flux<X> dematerialize() {
      return onAssembly(new FluxDematerialize<>(this));
   }

   public final Flux<T> distinct() {
      return this.distinct(identityFunction());
   }

   public final <V> Flux<T> distinct(Function<? super T, ? extends V> keySelector) {
      return this.distinct(keySelector, hashSetSupplier());
   }

   public final <V, C extends Collection<? super V>> Flux<T> distinct(Function<? super T, ? extends V> keySelector, Supplier<C> distinctCollectionSupplier) {
      return this.distinct(keySelector, distinctCollectionSupplier, Collection::add, Collection::clear);
   }

   public final <V, C> Flux<T> distinct(
      Function<? super T, ? extends V> keySelector, Supplier<C> distinctStoreSupplier, BiPredicate<C, V> distinctPredicate, Consumer<C> cleanup
   ) {
      return this instanceof Fuseable
         ? onAssembly(new FluxDistinctFuseable<>(this, keySelector, distinctStoreSupplier, distinctPredicate, cleanup))
         : onAssembly(new FluxDistinct<>(this, keySelector, distinctStoreSupplier, distinctPredicate, cleanup));
   }

   public final Flux<T> distinctUntilChanged() {
      return this.distinctUntilChanged(identityFunction());
   }

   public final <V> Flux<T> distinctUntilChanged(Function<? super T, ? extends V> keySelector) {
      return this.distinctUntilChanged(keySelector, equalPredicate());
   }

   public final <V> Flux<T> distinctUntilChanged(Function<? super T, ? extends V> keySelector, BiPredicate<? super V, ? super V> keyComparator) {
      return onAssembly(new FluxDistinctUntilChanged<>(this, keySelector, keyComparator));
   }

   public final Flux<T> doAfterTerminate(Runnable afterTerminate) {
      Objects.requireNonNull(afterTerminate, "afterTerminate");
      return doOnSignal(this, null, null, null, null, afterTerminate, null, null);
   }

   public final Flux<T> doOnCancel(Runnable onCancel) {
      Objects.requireNonNull(onCancel, "onCancel");
      return doOnSignal(this, null, null, null, null, null, null, onCancel);
   }

   public final Flux<T> doOnComplete(Runnable onComplete) {
      Objects.requireNonNull(onComplete, "onComplete");
      return doOnSignal(this, null, null, null, onComplete, null, null, null);
   }

   public final <R> Flux<T> doOnDiscard(Class<R> type, Consumer<? super R> discardHook) {
      return this.subscriberContext(Operators.discardLocalAdapter(type, discardHook));
   }

   public final Flux<T> doOnEach(Consumer<? super Signal<T>> signalConsumer) {
      return this instanceof Fuseable ? onAssembly(new FluxDoOnEachFuseable<>(this, signalConsumer)) : onAssembly(new FluxDoOnEach<>(this, signalConsumer));
   }

   public final Flux<T> doOnError(Consumer<? super Throwable> onError) {
      Objects.requireNonNull(onError, "onError");
      return doOnSignal(this, null, null, onError, null, null, null, null);
   }

   public final <E extends Throwable> Flux<T> doOnError(Class<E> exceptionType, Consumer<? super E> onError) {
      Objects.requireNonNull(exceptionType, "type");
      return this.doOnError(exceptionType::isInstance, onError);
   }

   public final Flux<T> doOnError(Predicate<? super Throwable> predicate, Consumer<? super Throwable> onError) {
      Objects.requireNonNull(predicate, "predicate");
      return this.doOnError(t -> {
         if (predicate.test(t)) {
            onError.accept(t);
         }

      });
   }

   public final Flux<T> doOnNext(Consumer<? super T> onNext) {
      Objects.requireNonNull(onNext, "onNext");
      return doOnSignal(this, null, onNext, null, null, null, null, null);
   }

   public final Flux<T> doOnRequest(LongConsumer consumer) {
      Objects.requireNonNull(consumer, "consumer");
      return doOnSignal(this, null, null, null, null, null, consumer, null);
   }

   public final Flux<T> doOnSubscribe(Consumer<? super Subscription> onSubscribe) {
      Objects.requireNonNull(onSubscribe, "onSubscribe");
      return doOnSignal(this, onSubscribe, null, null, null, null, null, null);
   }

   public final Flux<T> doOnTerminate(Runnable onTerminate) {
      Objects.requireNonNull(onTerminate, "onTerminate");
      return doOnSignal(this, null, null, e -> onTerminate.run(), onTerminate, null, null, null);
   }

   public final Flux<T> doFirst(Runnable onFirst) {
      Objects.requireNonNull(onFirst, "onFirst");
      return this instanceof Fuseable ? onAssembly(new FluxDoFirstFuseable<>(this, onFirst)) : onAssembly(new FluxDoFirst<>(this, onFirst));
   }

   public final Flux<T> doFinally(Consumer<SignalType> onFinally) {
      Objects.requireNonNull(onFinally, "onFinally");
      return this instanceof Fuseable ? onAssembly(new FluxDoFinallyFuseable<>(this, onFinally)) : onAssembly(new FluxDoFinally<>(this, onFinally));
   }

   public final Flux<Tuple2<Long, T>> elapsed() {
      return this.elapsed(Schedulers.parallel());
   }

   public final Flux<Tuple2<Long, T>> elapsed(Scheduler scheduler) {
      Objects.requireNonNull(scheduler, "scheduler");
      return onAssembly(new FluxElapsed<>(this, scheduler));
   }

   public final Mono<T> elementAt(int index) {
      return Mono.onAssembly(new MonoElementAt<>(this, (long)index));
   }

   public final Mono<T> elementAt(int index, T defaultValue) {
      return Mono.onAssembly(new MonoElementAt<>(this, (long)index, defaultValue));
   }

   public final Flux<T> expandDeep(Function<? super T, ? extends Publisher<? extends T>> expander, int capacityHint) {
      return onAssembly(new FluxExpand<>(this, expander, false, capacityHint));
   }

   public final Flux<T> expandDeep(Function<? super T, ? extends Publisher<? extends T>> expander) {
      return this.expandDeep(expander, Queues.SMALL_BUFFER_SIZE);
   }

   public final Flux<T> expand(Function<? super T, ? extends Publisher<? extends T>> expander, int capacityHint) {
      return onAssembly(new FluxExpand<>(this, expander, true, capacityHint));
   }

   public final Flux<T> expand(Function<? super T, ? extends Publisher<? extends T>> expander) {
      return this.expand(expander, Queues.SMALL_BUFFER_SIZE);
   }

   public final Flux<T> filter(Predicate<? super T> p) {
      return this instanceof Fuseable ? onAssembly(new FluxFilterFuseable<>(this, p)) : onAssembly(new FluxFilter<>(this, p));
   }

   public final Flux<T> filterWhen(Function<? super T, ? extends Publisher<Boolean>> asyncPredicate) {
      return this.filterWhen(asyncPredicate, Queues.SMALL_BUFFER_SIZE);
   }

   public final Flux<T> filterWhen(Function<? super T, ? extends Publisher<Boolean>> asyncPredicate, int bufferSize) {
      return onAssembly(new FluxFilterWhen<>(this, asyncPredicate, bufferSize));
   }

   public final <R> Flux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {
      return this.flatMap(mapper, Queues.SMALL_BUFFER_SIZE, Queues.XS_BUFFER_SIZE);
   }

   public final <V> Flux<V> flatMap(Function<? super T, ? extends Publisher<? extends V>> mapper, int concurrency) {
      return this.flatMap(mapper, concurrency, Queues.XS_BUFFER_SIZE);
   }

   public final <V> Flux<V> flatMap(Function<? super T, ? extends Publisher<? extends V>> mapper, int concurrency, int prefetch) {
      return this.flatMap(mapper, false, concurrency, prefetch);
   }

   public final <V> Flux<V> flatMapDelayError(Function<? super T, ? extends Publisher<? extends V>> mapper, int concurrency, int prefetch) {
      return this.flatMap(mapper, true, concurrency, prefetch);
   }

   public final <R> Flux<R> flatMap(
      @Nullable Function<? super T, ? extends Publisher<? extends R>> mapperOnNext,
      @Nullable Function<? super Throwable, ? extends Publisher<? extends R>> mapperOnError,
      @Nullable Supplier<? extends Publisher<? extends R>> mapperOnComplete
   ) {
      return onAssembly(
         new FluxFlatMap<>(
            new FluxMapSignal<>(this, mapperOnNext, mapperOnError, mapperOnComplete),
            identityFunction(),
            false,
            Queues.XS_BUFFER_SIZE,
            Queues.xs(),
            Queues.XS_BUFFER_SIZE,
            Queues.xs()
         )
      );
   }

   public final <R> Flux<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
      return this.flatMapIterable(mapper, Queues.SMALL_BUFFER_SIZE);
   }

   public final <R> Flux<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper, int prefetch) {
      return onAssembly(new FluxFlattenIterable<>(this, mapper, prefetch, Queues.get(prefetch)));
   }

   public final <R> Flux<R> flatMapSequential(Function<? super T, ? extends Publisher<? extends R>> mapper) {
      return this.flatMapSequential(mapper, Queues.SMALL_BUFFER_SIZE);
   }

   public final <R> Flux<R> flatMapSequential(Function<? super T, ? extends Publisher<? extends R>> mapper, int maxConcurrency) {
      return this.flatMapSequential(mapper, maxConcurrency, Queues.XS_BUFFER_SIZE);
   }

   public final <R> Flux<R> flatMapSequential(Function<? super T, ? extends Publisher<? extends R>> mapper, int maxConcurrency, int prefetch) {
      return this.flatMapSequential(mapper, false, maxConcurrency, prefetch);
   }

   public final <R> Flux<R> flatMapSequentialDelayError(Function<? super T, ? extends Publisher<? extends R>> mapper, int maxConcurrency, int prefetch) {
      return this.flatMapSequential(mapper, true, maxConcurrency, prefetch);
   }

   public int getPrefetch() {
      return -1;
   }

   public final <K> Flux<GroupedFlux<K, T>> groupBy(Function<? super T, ? extends K> keyMapper) {
      return this.groupBy(keyMapper, identityFunction());
   }

   public final <K> Flux<GroupedFlux<K, T>> groupBy(Function<? super T, ? extends K> keyMapper, int prefetch) {
      return this.groupBy(keyMapper, identityFunction(), prefetch);
   }

   public final <K, V> Flux<GroupedFlux<K, V>> groupBy(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
      return this.groupBy(keyMapper, valueMapper, Queues.SMALL_BUFFER_SIZE);
   }

   public final <K, V> Flux<GroupedFlux<K, V>> groupBy(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper, int prefetch) {
      return onAssembly(new FluxGroupBy<>(this, keyMapper, valueMapper, Queues.unbounded(prefetch), Queues.unbounded(prefetch), prefetch));
   }

   public final <TRight, TLeftEnd, TRightEnd, R> Flux<R> groupJoin(
      Publisher<? extends TRight> other,
      Function<? super T, ? extends Publisher<TLeftEnd>> leftEnd,
      Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd,
      BiFunction<? super T, ? super Flux<TRight>, ? extends R> resultSelector
   ) {
      return onAssembly(
         new FluxGroupJoin<>(this, other, leftEnd, rightEnd, resultSelector, Queues.unbounded(Queues.XS_BUFFER_SIZE), Queues.unbounded(Queues.XS_BUFFER_SIZE))
      );
   }

   public final <R> Flux<R> handle(BiConsumer<? super T, SynchronousSink<R>> handler) {
      return this instanceof Fuseable ? onAssembly(new FluxHandleFuseable<>(this, handler)) : onAssembly(new FluxHandle<>(this, handler));
   }

   public final Mono<Boolean> hasElement(T value) {
      Objects.requireNonNull(value, "value");
      return this.any(t -> Objects.equals(value, t));
   }

   public final Mono<Boolean> hasElements() {
      return Mono.onAssembly(new MonoHasElements<>(this));
   }

   public Flux<T> hide() {
      return new FluxHide<>(this);
   }

   public final Flux<Tuple2<Long, T>> index() {
      return this.index(tuple2Function());
   }

   public final <I> Flux<I> index(BiFunction<? super Long, ? super T, ? extends I> indexMapper) {
      return this instanceof Fuseable ? onAssembly(new FluxIndexFuseable<>(this, indexMapper)) : onAssembly(new FluxIndex<>(this, indexMapper));
   }

   public final Mono<T> ignoreElements() {
      return Mono.onAssembly(new MonoIgnoreElements<>(this));
   }

   public final <TRight, TLeftEnd, TRightEnd, R> Flux<R> join(
      Publisher<? extends TRight> other,
      Function<? super T, ? extends Publisher<TLeftEnd>> leftEnd,
      Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd,
      BiFunction<? super T, ? super TRight, ? extends R> resultSelector
   ) {
      return onAssembly(new FluxJoin<>(this, other, leftEnd, rightEnd, resultSelector));
   }

   public final Mono<T> last() {
      if (this instanceof Callable) {
         Callable<T> thiz = (Callable)this;
         Mono<T> callableMono = wrapToMono(thiz);
         return callableMono == Mono.empty()
            ? Mono.onAssembly(new MonoError<>(new NoSuchElementException("Flux#last() didn't observe any onNext signal from Callable flux")))
            : Mono.onAssembly(callableMono);
      } else {
         return Mono.onAssembly(new MonoTakeLastOne<>(this));
      }
   }

   public final Mono<T> last(T defaultValue) {
      if (this instanceof Callable) {
         Callable<T> thiz = (Callable)this;
         if (thiz instanceof Fuseable.ScalarCallable) {
            Fuseable.ScalarCallable<T> c = (Fuseable.ScalarCallable)thiz;

            T v;
            try {
               v = (T)c.call();
            } catch (Exception var6) {
               return Mono.error(Exceptions.unwrap(var6));
            }

            if (v == null) {
               return Mono.just(defaultValue);
            }

            return Mono.just(v);
         }

         Mono.onAssembly(new MonoCallable<>(thiz));
      }

      return Mono.onAssembly(new MonoTakeLastOne<>(this, defaultValue));
   }

   public final Flux<T> limitRate(int prefetchRate) {
      return onAssembly(this.publishOn(Schedulers.immediate(), prefetchRate));
   }

   public final Flux<T> limitRate(int highTide, int lowTide) {
      return onAssembly(this.publishOn(Schedulers.immediate(), true, highTide, lowTide));
   }

   @Deprecated
   public final Flux<T> limitRequest(long n) {
      return this.take(n, true);
   }

   public final Flux<T> log() {
      return this.log(null, Level.INFO);
   }

   public final Flux<T> log(String category) {
      return this.log(category, Level.INFO);
   }

   public final Flux<T> log(@Nullable String category, Level level, SignalType... options) {
      return this.log(category, level, false, options);
   }

   public final Flux<T> log(@Nullable String category, Level level, boolean showOperatorLine, SignalType... options) {
      SignalLogger<T> log = new SignalLogger<>(this, category, level, showOperatorLine, options);
      return this instanceof Fuseable ? onAssembly(new FluxLogFuseable<>(this, log)) : onAssembly(new FluxLog<>(this, log));
   }

   public final Flux<T> log(Logger logger) {
      return this.log(logger, Level.INFO, false);
   }

   public final Flux<T> log(Logger logger, Level level, boolean showOperatorLine, SignalType... options) {
      SignalLogger<T> log = new SignalLogger<>(this, "IGNORED", level, showOperatorLine, s -> logger, options);
      return this instanceof Fuseable ? onAssembly(new FluxLogFuseable<>(this, log)) : onAssembly(new FluxLog<>(this, log));
   }

   public final <V> Flux<V> map(Function<? super T, ? extends V> mapper) {
      return this instanceof Fuseable ? onAssembly(new FluxMapFuseable<>(this, mapper)) : onAssembly(new FluxMap<>(this, mapper));
   }

   public final <V> Flux<V> mapNotNull(Function<? super T, ? extends V> mapper) {
      return this.handle((t, sink) -> {
         V v = (V)mapper.apply(t);
         if (v != null) {
            sink.next(v);
         }

      });
   }

   public final Flux<Signal<T>> materialize() {
      return onAssembly(new FluxMaterialize<>(this));
   }

   @Deprecated
   public final Flux<T> mergeOrderedWith(Publisher<? extends T> other, Comparator<? super T> otherComparator) {
      if (this instanceof FluxMergeComparing) {
         FluxMergeComparing<T> fluxMerge = (FluxMergeComparing)this;
         return fluxMerge.mergeAdditionalSource(other, otherComparator);
      } else {
         return mergeOrdered(otherComparator, this, other);
      }
   }

   public final Flux<T> mergeComparingWith(Publisher<? extends T> other, Comparator<? super T> otherComparator) {
      if (this instanceof FluxMergeComparing) {
         FluxMergeComparing<T> fluxMerge = (FluxMergeComparing)this;
         return fluxMerge.mergeAdditionalSource(other, otherComparator);
      } else {
         return mergeComparing(otherComparator, this, other);
      }
   }

   public final Flux<T> mergeWith(Publisher<? extends T> other) {
      if (this instanceof FluxMerge) {
         FluxMerge<T> fluxMerge = (FluxMerge)this;
         return fluxMerge.mergeAdditionalSource(other, Queues::get);
      } else {
         return merge(this, other);
      }
   }

   public final Flux<T> metrics() {
      if (!Metrics.isInstrumentationAvailable()) {
         return this;
      } else {
         return this instanceof Fuseable ? onAssembly(new FluxMetricsFuseable<>(this)) : onAssembly(new FluxMetrics<>(this));
      }
   }

   public final Flux<T> name(String name) {
      return FluxName.createOrAppend(this, name);
   }

   public final Mono<T> next() {
      if (this instanceof Callable) {
         Callable<T> m = (Callable)this;
         return Mono.onAssembly(wrapToMono(m));
      } else {
         return Mono.onAssembly(new MonoNext<>(this));
      }
   }

   public final <U> Flux<U> ofType(Class<U> clazz) {
      Objects.requireNonNull(clazz, "clazz");
      return this.filter(o -> clazz.isAssignableFrom(o.getClass())).cast(clazz);
   }

   public final Flux<T> onBackpressureBuffer() {
      return onAssembly(new FluxOnBackpressureBuffer<>(this, Queues.SMALL_BUFFER_SIZE, true, null));
   }

   public final Flux<T> onBackpressureBuffer(int maxSize) {
      return onAssembly(new FluxOnBackpressureBuffer<>(this, maxSize, false, null));
   }

   public final Flux<T> onBackpressureBuffer(int maxSize, Consumer<? super T> onOverflow) {
      Objects.requireNonNull(onOverflow, "onOverflow");
      return onAssembly(new FluxOnBackpressureBuffer<>(this, maxSize, false, onOverflow));
   }

   public final Flux<T> onBackpressureBuffer(int maxSize, BufferOverflowStrategy bufferOverflowStrategy) {
      Objects.requireNonNull(bufferOverflowStrategy, "bufferOverflowStrategy");
      return onAssembly(new FluxOnBackpressureBufferStrategy<>(this, maxSize, null, bufferOverflowStrategy));
   }

   public final Flux<T> onBackpressureBuffer(int maxSize, Consumer<? super T> onBufferOverflow, BufferOverflowStrategy bufferOverflowStrategy) {
      Objects.requireNonNull(onBufferOverflow, "onBufferOverflow");
      Objects.requireNonNull(bufferOverflowStrategy, "bufferOverflowStrategy");
      return onAssembly(new FluxOnBackpressureBufferStrategy<>(this, maxSize, onBufferOverflow, bufferOverflowStrategy));
   }

   public final Flux<T> onBackpressureBuffer(Duration ttl, int maxSize, Consumer<? super T> onBufferEviction) {
      return this.onBackpressureBuffer(ttl, maxSize, onBufferEviction, Schedulers.parallel());
   }

   public final Flux<T> onBackpressureBuffer(Duration ttl, int maxSize, Consumer<? super T> onBufferEviction, Scheduler scheduler) {
      Objects.requireNonNull(ttl, "ttl");
      Objects.requireNonNull(onBufferEviction, "onBufferEviction");
      return onAssembly(new FluxOnBackpressureBufferTimeout<>(this, ttl, scheduler, maxSize, onBufferEviction));
   }

   public final Flux<T> onBackpressureDrop() {
      return onAssembly(new FluxOnBackpressureDrop<>(this));
   }

   public final Flux<T> onBackpressureDrop(Consumer<? super T> onDropped) {
      return onAssembly(new FluxOnBackpressureDrop<>(this, onDropped));
   }

   public final Flux<T> onBackpressureError() {
      return this.onBackpressureDrop(t -> {
         throw Exceptions.failWithOverflow();
      });
   }

   public final Flux<T> onBackpressureLatest() {
      return onAssembly(new FluxOnBackpressureLatest<>(this));
   }

   public final Flux<T> onErrorContinue(BiConsumer<Throwable, Object> errorConsumer) {
      return this.subscriberContext(Context.of("reactor.onNextError.localStrategy", OnNextFailureStrategy.resume(errorConsumer)));
   }

   public final <E extends Throwable> Flux<T> onErrorContinue(Class<E> type, BiConsumer<Throwable, Object> errorConsumer) {
      return this.onErrorContinue(type::isInstance, errorConsumer);
   }

   public final <E extends Throwable> Flux<T> onErrorContinue(Predicate<E> errorPredicate, BiConsumer<Throwable, Object> errorConsumer) {
      return this.subscriberContext(Context.of("reactor.onNextError.localStrategy", OnNextFailureStrategy.resumeIf(errorPredicate, errorConsumer)));
   }

   public final Flux<T> onErrorStop() {
      return this.subscriberContext(Context.of("reactor.onNextError.localStrategy", OnNextFailureStrategy.stop()));
   }

   public final Flux<T> onErrorMap(Function<? super Throwable, ? extends Throwable> mapper) {
      return this.onErrorResume(e -> Mono.error((Throwable)mapper.apply(e)));
   }

   public final <E extends Throwable> Flux<T> onErrorMap(Class<E> type, Function<? super E, ? extends Throwable> mapper) {
      return this.onErrorMap(type::isInstance, mapper);
   }

   public final Flux<T> onErrorMap(Predicate<? super Throwable> predicate, Function<? super Throwable, ? extends Throwable> mapper) {
      return this.onErrorResume(predicate, e -> Mono.error((Throwable)mapper.apply(e)));
   }

   public final Flux<T> onErrorResume(Function<? super Throwable, ? extends Publisher<? extends T>> fallback) {
      return onAssembly(new FluxOnErrorResume<>(this, fallback));
   }

   public final <E extends Throwable> Flux<T> onErrorResume(Class<E> type, Function<? super E, ? extends Publisher<? extends T>> fallback) {
      Objects.requireNonNull(type, "type");
      return this.onErrorResume(type::isInstance, fallback);
   }

   public final Flux<T> onErrorResume(Predicate<? super Throwable> predicate, Function<? super Throwable, ? extends Publisher<? extends T>> fallback) {
      Objects.requireNonNull(predicate, "predicate");
      return this.onErrorResume(e -> (Publisher)(predicate.test(e) ? (Publisher)fallback.apply(e) : error(e)));
   }

   public final Flux<T> onErrorReturn(T fallbackValue) {
      return this.onErrorResume(t -> just(fallbackValue));
   }

   public final <E extends Throwable> Flux<T> onErrorReturn(Class<E> type, T fallbackValue) {
      return this.onErrorResume(type, t -> just(fallbackValue));
   }

   public final Flux<T> onErrorReturn(Predicate<? super Throwable> predicate, T fallbackValue) {
      return this.onErrorResume(predicate, t -> just(fallbackValue));
   }

   public final Flux<T> onTerminateDetach() {
      return new FluxDetach<>(this);
   }

   public final Flux<T> or(Publisher<? extends T> other) {
      if (this instanceof FluxFirstWithSignal) {
         FluxFirstWithSignal<T> orPublisher = (FluxFirstWithSignal)this;
         FluxFirstWithSignal<T> result = orPublisher.orAdditionalSource(other);
         if (result != null) {
            return result;
         }
      }

      return firstWithSignal(this, other);
   }

   public final ParallelFlux<T> parallel() {
      return this.parallel(Schedulers.DEFAULT_POOL_SIZE);
   }

   public final ParallelFlux<T> parallel(int parallelism) {
      return this.parallel(parallelism, Queues.SMALL_BUFFER_SIZE);
   }

   public final ParallelFlux<T> parallel(int parallelism, int prefetch) {
      return ParallelFlux.from(this, parallelism, prefetch, Queues.get(prefetch));
   }

   public final ConnectableFlux<T> publish() {
      return this.publish(Queues.SMALL_BUFFER_SIZE);
   }

   public final ConnectableFlux<T> publish(int prefetch) {
      return onAssembly(new FluxPublish<>(this, prefetch, Queues.get(prefetch)));
   }

   public final <R> Flux<R> publish(Function<? super Flux<T>, ? extends Publisher<? extends R>> transform) {
      return this.publish(transform, Queues.SMALL_BUFFER_SIZE);
   }

   public final <R> Flux<R> publish(Function<? super Flux<T>, ? extends Publisher<? extends R>> transform, int prefetch) {
      return onAssembly(new FluxPublishMulticast<>(this, transform, prefetch, Queues.get(prefetch)));
   }

   @Deprecated
   public final Mono<T> publishNext() {
      return this.shareNext();
   }

   public final Flux<T> publishOn(Scheduler scheduler) {
      return this.publishOn(scheduler, Queues.SMALL_BUFFER_SIZE);
   }

   public final Flux<T> publishOn(Scheduler scheduler, int prefetch) {
      return this.publishOn(scheduler, true, prefetch);
   }

   public final Flux<T> publishOn(Scheduler scheduler, boolean delayError, int prefetch) {
      return this.publishOn(scheduler, delayError, prefetch, prefetch);
   }

   final Flux<T> publishOn(Scheduler scheduler, boolean delayError, int prefetch, int lowTide) {
      if (!(this instanceof Callable)) {
         return onAssembly(new FluxPublishOn<>(this, scheduler, delayError, prefetch, lowTide, Queues.get(prefetch)));
      } else {
         if (this instanceof Fuseable.ScalarCallable) {
            Fuseable.ScalarCallable<T> s = (Fuseable.ScalarCallable)this;

            try {
               return onAssembly(new FluxSubscribeOnValue<>((T)s.call(), scheduler));
            } catch (Exception var7) {
            }
         }

         Callable<T> c = (Callable)this;
         return onAssembly(new FluxSubscribeOnCallable<>(c, scheduler));
      }
   }

   public final Mono<T> reduce(BiFunction<T, T, T> aggregator) {
      if (this instanceof Callable) {
         Callable<T> thiz = (Callable)this;
         return Mono.onAssembly(wrapToMono(thiz));
      } else {
         return Mono.onAssembly(new MonoReduce<>(this, aggregator));
      }
   }

   public final <A> Mono<A> reduce(A initial, BiFunction<A, ? super T, A> accumulator) {
      return this.reduceWith(() -> initial, accumulator);
   }

   public final <A> Mono<A> reduceWith(Supplier<A> initial, BiFunction<A, ? super T, A> accumulator) {
      return Mono.onAssembly(new MonoReduceSeed<>(this, initial, accumulator));
   }

   public final Flux<T> repeat() {
      return this.repeat(ALWAYS_BOOLEAN_SUPPLIER);
   }

   public final Flux<T> repeat(BooleanSupplier predicate) {
      return onAssembly(new FluxRepeatPredicate<>(this, predicate));
   }

   public final Flux<T> repeat(long numRepeat) {
      return numRepeat == 0L ? this : onAssembly(new FluxRepeat<>(this, numRepeat));
   }

   public final Flux<T> repeat(long numRepeat, BooleanSupplier predicate) {
      if (numRepeat < 0L) {
         throw new IllegalArgumentException("numRepeat >= 0 required");
      } else {
         return numRepeat == 0L ? this : defer(() -> this.repeat(countingBooleanSupplier(predicate, numRepeat)));
      }
   }

   public final Flux<T> repeatWhen(Function<Flux<Long>, ? extends Publisher<?>> repeatFactory) {
      return onAssembly(new FluxRepeatWhen<>(this, repeatFactory));
   }

   public final ConnectableFlux<T> replay() {
      return this.replay(Integer.MAX_VALUE);
   }

   public final ConnectableFlux<T> replay(int history) {
      return history == 0
         ? onAssembly(new FluxPublish<>(this, Queues.SMALL_BUFFER_SIZE, Queues.get(Queues.SMALL_BUFFER_SIZE)))
         : onAssembly(new FluxReplay<>(this, history, 0L, null));
   }

   public final ConnectableFlux<T> replay(Duration ttl) {
      return this.replay(Integer.MAX_VALUE, ttl);
   }

   public final ConnectableFlux<T> replay(int history, Duration ttl) {
      return this.replay(history, ttl, Schedulers.parallel());
   }

   public final ConnectableFlux<T> replay(Duration ttl, Scheduler timer) {
      return this.replay(Integer.MAX_VALUE, ttl, timer);
   }

   public final ConnectableFlux<T> replay(int history, Duration ttl, Scheduler timer) {
      Objects.requireNonNull(timer, "timer");
      return history == 0
         ? onAssembly(new FluxPublish<>(this, Queues.SMALL_BUFFER_SIZE, Queues.get(Queues.SMALL_BUFFER_SIZE)))
         : onAssembly(new FluxReplay<>(this, history, ttl.toNanos(), timer));
   }

   public final Flux<T> retry() {
      return this.retry(Long.MAX_VALUE);
   }

   public final Flux<T> retry(long numRetries) {
      return onAssembly(new FluxRetry<>(this, numRetries));
   }

   public final Flux<T> retryWhen(Retry retrySpec) {
      return onAssembly(new FluxRetryWhen<>(this, retrySpec));
   }

   public final Flux<T> sample(Duration timespan) {
      return this.sample(interval(timespan));
   }

   public final <U> Flux<T> sample(Publisher<U> sampler) {
      return onAssembly(new FluxSample<>(this, sampler));
   }

   public final Flux<T> sampleFirst(Duration timespan) {
      return this.sampleFirst((Function)(t -> Mono.delay(timespan)));
   }

   public final <U> Flux<T> sampleFirst(Function<? super T, ? extends Publisher<U>> samplerFactory) {
      return onAssembly(new FluxSampleFirst<>(this, samplerFactory));
   }

   public final <U> Flux<T> sampleTimeout(Function<? super T, ? extends Publisher<U>> throttlerFactory) {
      return this.sampleTimeout(throttlerFactory, Queues.XS_BUFFER_SIZE);
   }

   public final <U> Flux<T> sampleTimeout(Function<? super T, ? extends Publisher<U>> throttlerFactory, int maxConcurrency) {
      return onAssembly(new FluxSampleTimeout<>(this, throttlerFactory, Queues.get(maxConcurrency)));
   }

   public final Flux<T> scan(BiFunction<T, T, T> accumulator) {
      return onAssembly(new FluxScan<>(this, accumulator));
   }

   public final <A> Flux<A> scan(A initial, BiFunction<A, ? super T, A> accumulator) {
      Objects.requireNonNull(initial, "seed");
      return this.scanWith(() -> initial, accumulator);
   }

   public final <A> Flux<A> scanWith(Supplier<A> initial, BiFunction<A, ? super T, A> accumulator) {
      return onAssembly(new FluxScanSeed<>(this, initial, accumulator));
   }

   public final Flux<T> share() {
      return onAssembly(new FluxRefCount<>(new FluxPublish<>(this, Queues.SMALL_BUFFER_SIZE, Queues.small()), 1));
   }

   public final Mono<T> shareNext() {
      NextProcessor<T> nextProcessor = new NextProcessor<>(this);
      return Mono.onAssembly(nextProcessor);
   }

   public final Mono<T> single() {
      if (this instanceof Callable) {
         if (this instanceof Fuseable.ScalarCallable) {
            Fuseable.ScalarCallable<T> scalarCallable = (Fuseable.ScalarCallable)this;

            T v;
            try {
               v = (T)scalarCallable.call();
            } catch (Exception var4) {
               return Mono.error(Exceptions.unwrap(var4));
            }

            return v == null ? Mono.error(new NoSuchElementException("Source was a (constant) empty")) : Mono.just(v);
         } else {
            Callable<T> thiz = (Callable)this;
            return Mono.onAssembly(new MonoSingleCallable<>(thiz));
         }
      } else {
         return Mono.onAssembly(new MonoSingle<>(this));
      }
   }

   public final Mono<T> single(T defaultValue) {
      if (this instanceof Callable) {
         if (this instanceof Fuseable.ScalarCallable) {
            Fuseable.ScalarCallable<T> scalarCallable = (Fuseable.ScalarCallable)this;

            T v;
            try {
               v = (T)scalarCallable.call();
            } catch (Exception var5) {
               return Mono.error(Exceptions.unwrap(var5));
            }

            return v == null ? Mono.just(defaultValue) : Mono.just(v);
         } else {
            Callable<T> thiz = (Callable)this;
            return Mono.onAssembly(new MonoSingleCallable<>(thiz, defaultValue));
         }
      } else {
         return Mono.onAssembly(new MonoSingle<>(this, defaultValue, false));
      }
   }

   public final Mono<T> singleOrEmpty() {
      if (this instanceof Callable) {
         Callable<T> thiz = (Callable)this;
         return Mono.onAssembly(wrapToMono(thiz));
      } else {
         return Mono.onAssembly(new MonoSingle<>(this, (T)null, true));
      }
   }

   public final Flux<T> skip(long skipped) {
      return skipped == 0L ? this : onAssembly(new FluxSkip<>(this, skipped));
   }

   public final Flux<T> skip(Duration timespan) {
      return this.skip(timespan, Schedulers.parallel());
   }

   public final Flux<T> skip(Duration timespan, Scheduler timer) {
      return !timespan.isZero() ? this.skipUntilOther(Mono.delay(timespan, timer)) : this;
   }

   public final Flux<T> skipLast(int n) {
      return n == 0 ? this : onAssembly(new FluxSkipLast<>(this, n));
   }

   public final Flux<T> skipUntil(Predicate<? super T> untilPredicate) {
      return onAssembly(new FluxSkipUntil<>(this, untilPredicate));
   }

   public final Flux<T> skipUntilOther(Publisher<?> other) {
      return onAssembly(new FluxSkipUntilOther<>(this, other));
   }

   public final Flux<T> skipWhile(Predicate<? super T> skipPredicate) {
      return onAssembly(new FluxSkipWhile<>(this, skipPredicate));
   }

   public final Flux<T> sort() {
      return this.collectSortedList().flatMapIterable(identityFunction());
   }

   public final Flux<T> sort(Comparator<? super T> sortFunction) {
      return this.collectSortedList(sortFunction).flatMapIterable(identityFunction());
   }

   public final Flux<T> startWith(Iterable<? extends T> iterable) {
      return this.startWith(fromIterable(iterable));
   }

   @SafeVarargs
   public final Flux<T> startWith(T... values) {
      return this.startWith(just(values));
   }

   public final Flux<T> startWith(Publisher<? extends T> publisher) {
      if (this instanceof FluxConcatArray) {
         FluxConcatArray<T> fluxConcatArray = (FluxConcatArray)this;
         return fluxConcatArray.concatAdditionalSourceFirst(publisher);
      } else {
         return concat(publisher, this);
      }
   }

   public final Disposable subscribe() {
      return this.subscribe(null, null, null);
   }

   public final Disposable subscribe(Consumer<? super T> consumer) {
      Objects.requireNonNull(consumer, "consumer");
      return this.subscribe(consumer, null, null);
   }

   public final Disposable subscribe(@Nullable Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer) {
      Objects.requireNonNull(errorConsumer, "errorConsumer");
      return this.subscribe(consumer, errorConsumer, null);
   }

   public final Disposable subscribe(
      @Nullable Consumer<? super T> consumer, @Nullable Consumer<? super Throwable> errorConsumer, @Nullable Runnable completeConsumer
   ) {
      return this.subscribe(consumer, errorConsumer, completeConsumer, (Context)null);
   }

   @Deprecated
   public final Disposable subscribe(
      @Nullable Consumer<? super T> consumer,
      @Nullable Consumer<? super Throwable> errorConsumer,
      @Nullable Runnable completeConsumer,
      @Nullable Consumer<? super Subscription> subscriptionConsumer
   ) {
      return this.subscribeWith(new LambdaSubscriber(consumer, errorConsumer, completeConsumer, subscriptionConsumer, null));
   }

   public final Disposable subscribe(
      @Nullable Consumer<? super T> consumer,
      @Nullable Consumer<? super Throwable> errorConsumer,
      @Nullable Runnable completeConsumer,
      @Nullable Context initialContext
   ) {
      return this.subscribeWith(new LambdaSubscriber(consumer, errorConsumer, completeConsumer, null, initialContext));
   }

   @Override
   public final void subscribe(Subscriber<? super T> actual) {
      CorePublisher publisher = Operators.onLastAssembly(this);
      CoreSubscriber subscriber = Operators.toCoreSubscriber(actual);

      try {
         if (publisher instanceof OptimizableOperator) {
            OptimizableOperator operator = (OptimizableOperator)publisher;

            while(true) {
               subscriber = operator.subscribeOrReturn(subscriber);
               if (subscriber == null) {
                  return;
               }

               OptimizableOperator newSource = operator.nextOptimizableSource();
               if (newSource == null) {
                  publisher = operator.source();
                  break;
               }

               operator = newSource;
            }
         }

         publisher.subscribe(subscriber);
      } catch (Throwable var6) {
         Operators.reportThrowInSubscribe(subscriber, var6);
      }
   }

   @Override
   public abstract void subscribe(CoreSubscriber<? super T> var1);

   @Deprecated
   public final Flux<T> subscriberContext(Context mergeContext) {
      return this.subscriberContext(c -> c.putAll(mergeContext.readOnly()));
   }

   @Deprecated
   public final Flux<T> subscriberContext(Function<Context, Context> doOnContext) {
      return this.contextWrite(doOnContext);
   }

   public final Flux<T> subscribeOn(Scheduler scheduler) {
      return this.subscribeOn(scheduler, true);
   }

   public final Flux<T> subscribeOn(Scheduler scheduler, boolean requestOnSeparateThread) {
      if (!(this instanceof Callable)) {
         return onAssembly(new FluxSubscribeOn<>(this, scheduler, requestOnSeparateThread));
      } else {
         if (this instanceof Fuseable.ScalarCallable) {
            try {
               T value = (T)((Fuseable.ScalarCallable)this).call();
               return onAssembly(new FluxSubscribeOnValue<>(value, scheduler));
            } catch (Exception var4) {
            }
         }

         Callable<T> c = (Callable)this;
         return onAssembly(new FluxSubscribeOnCallable<>(c, scheduler));
      }
   }

   public final <E extends Subscriber<? super T>> E subscribeWith(E subscriber) {
      this.subscribe(subscriber);
      return subscriber;
   }

   public final <V> Flux<V> switchOnFirst(BiFunction<Signal<? extends T>, Flux<T>, Publisher<? extends V>> transformer) {
      return this.switchOnFirst(transformer, true);
   }

   public final <V> Flux<V> switchOnFirst(BiFunction<Signal<? extends T>, Flux<T>, Publisher<? extends V>> transformer, boolean cancelSourceOnComplete) {
      return onAssembly(new FluxSwitchOnFirst<>(this, transformer, cancelSourceOnComplete));
   }

   public final Flux<T> switchIfEmpty(Publisher<? extends T> alternate) {
      return onAssembly(new FluxSwitchIfEmpty<>(this, alternate));
   }

   public final <V> Flux<V> switchMap(Function<? super T, Publisher<? extends V>> fn) {
      return this.switchMap(fn, Queues.XS_BUFFER_SIZE);
   }

   @Deprecated
   public final <V> Flux<V> switchMap(Function<? super T, Publisher<? extends V>> fn, int prefetch) {
      return prefetch == 0
         ? onAssembly(new FluxSwitchMapNoPrefetch<>(this, fn))
         : onAssembly(new FluxSwitchMap<>(this, fn, Queues.unbounded(prefetch), prefetch));
   }

   public final Flux<T> tag(String key, String value) {
      return FluxName.createOrAppend(this, key, value);
   }

   public final Flux<T> take(long n) {
      return this.take(n, false);
   }

   public final Flux<T> take(long n, boolean limitRequest) {
      if (limitRequest) {
         return onAssembly(new FluxLimitRequest<>(this, n));
      } else {
         return this instanceof Fuseable ? onAssembly(new FluxTakeFuseable<>(this, n)) : onAssembly(new FluxTake<>(this, n));
      }
   }

   public final Flux<T> take(Duration timespan) {
      return this.take(timespan, Schedulers.parallel());
   }

   public final Flux<T> take(Duration timespan, Scheduler timer) {
      return !timespan.isZero() ? this.takeUntilOther(Mono.delay(timespan, timer)) : this.take(0L);
   }

   public final Flux<T> takeLast(int n) {
      return n == 1 ? onAssembly(new FluxTakeLastOne<>(this)) : onAssembly(new FluxTakeLast<>(this, n));
   }

   public final Flux<T> takeUntil(Predicate<? super T> predicate) {
      return onAssembly(new FluxTakeUntil<>(this, predicate));
   }

   public final Flux<T> takeUntilOther(Publisher<?> other) {
      return onAssembly(new FluxTakeUntilOther<>(this, other));
   }

   public final Flux<T> takeWhile(Predicate<? super T> continuePredicate) {
      return onAssembly(new FluxTakeWhile<>(this, continuePredicate));
   }

   public final Mono<Void> then() {
      Mono<Void> then = new MonoIgnoreElements<>(this);
      return Mono.onAssembly(then);
   }

   public final <V> Mono<V> then(Mono<V> other) {
      return Mono.onAssembly(new MonoIgnoreThen<>(new Publisher[]{this}, other));
   }

   public final Mono<Void> thenEmpty(Publisher<Void> other) {
      return this.then(Mono.fromDirect(other));
   }

   public final <V> Flux<V> thenMany(Publisher<V> other) {
      if (this instanceof FluxConcatArray) {
         FluxConcatArray<T> fluxConcatArray = (FluxConcatArray)this;
         return fluxConcatArray.concatAdditionalIgnoredLast(other);
      } else {
         return concat(this.ignoreElements(), other);
      }
   }

   public final Flux<Timed<T>> timed() {
      return this.timed(Schedulers.parallel());
   }

   public final Flux<Timed<T>> timed(Scheduler clock) {
      return onAssembly(new FluxTimed<>(this, clock));
   }

   public final Flux<T> timeout(Duration timeout) {
      return this.timeout(timeout, null, Schedulers.parallel());
   }

   public final Flux<T> timeout(Duration timeout, @Nullable Publisher<? extends T> fallback) {
      return this.timeout(timeout, fallback, Schedulers.parallel());
   }

   public final Flux<T> timeout(Duration timeout, Scheduler timer) {
      return this.timeout(timeout, null, timer);
   }

   public final Flux<T> timeout(Duration timeout, @Nullable Publisher<? extends T> fallback, Scheduler timer) {
      Mono<Long> _timer = Mono.delay(timeout, timer).onErrorReturn((T)0L);
      Function<T, Publisher<Long>> rest = o -> _timer;
      return fallback == null ? this.timeout(_timer, rest, timeout.toMillis() + "ms") : this.timeout(_timer, rest, fallback);
   }

   public final <U> Flux<T> timeout(Publisher<U> firstTimeout) {
      return this.timeout(firstTimeout, t -> never());
   }

   public final <U, V> Flux<T> timeout(Publisher<U> firstTimeout, Function<? super T, ? extends Publisher<V>> nextTimeoutFactory) {
      return this.timeout(firstTimeout, nextTimeoutFactory, "first signal from a Publisher");
   }

   private final <U, V> Flux<T> timeout(Publisher<U> firstTimeout, Function<? super T, ? extends Publisher<V>> nextTimeoutFactory, String timeoutDescription) {
      return onAssembly(new FluxTimeout<>(this, firstTimeout, nextTimeoutFactory, timeoutDescription));
   }

   public final <U, V> Flux<T> timeout(
      Publisher<U> firstTimeout, Function<? super T, ? extends Publisher<V>> nextTimeoutFactory, Publisher<? extends T> fallback
   ) {
      return onAssembly(new FluxTimeout<>(this, firstTimeout, nextTimeoutFactory, fallback));
   }

   public final Flux<Tuple2<Long, T>> timestamp() {
      return this.timestamp(Schedulers.parallel());
   }

   public final Flux<Tuple2<Long, T>> timestamp(Scheduler scheduler) {
      Objects.requireNonNull(scheduler, "scheduler");
      return this.map(d -> Tuples.of(scheduler.now(TimeUnit.MILLISECONDS), d));
   }

   public final Iterable<T> toIterable() {
      return this.toIterable(Queues.SMALL_BUFFER_SIZE);
   }

   public final Iterable<T> toIterable(int batchSize) {
      return this.toIterable(batchSize, null);
   }

   public final Iterable<T> toIterable(int batchSize, @Nullable Supplier<Queue<T>> queueProvider) {
      Supplier<Queue<T>> provider;
      if (queueProvider == null) {
         provider = Queues.get(batchSize);
      } else {
         provider = () -> Hooks.wrapQueue((Queue<T>)queueProvider.get());
      }

      return new BlockingIterable<>(this, batchSize, provider);
   }

   public final Stream<T> toStream() {
      return this.toStream(Queues.SMALL_BUFFER_SIZE);
   }

   public final Stream<T> toStream(int batchSize) {
      Supplier<Queue<T>> provider = Queues.get(batchSize);
      return new BlockingIterable<>(this, batchSize, provider).stream();
   }

   public final <V> Flux<V> transform(Function<? super Flux<T>, ? extends Publisher<V>> transformer) {
      if (Hooks.DETECT_CONTEXT_LOSS) {
         transformer = new ContextTrackingFunctionWrapper<>(transformer);
      }

      return onAssembly(from((Publisher<? extends V>)transformer.apply(this)));
   }

   public final <V> Flux<V> transformDeferred(Function<? super Flux<T>, ? extends Publisher<V>> transformer) {
      return defer(() -> {
         if (Hooks.DETECT_CONTEXT_LOSS) {
            ContextTrackingFunctionWrapper<T, V> wrapper = new ContextTrackingFunctionWrapper<>(transformer);
            return wrapper.apply(this);
         } else {
            return (Publisher)transformer.apply(this);
         }
      });
   }

   public final <V> Flux<V> transformDeferredContextual(BiFunction<? super Flux<T>, ? super ContextView, ? extends Publisher<V>> transformer) {
      return deferContextual(
         ctxView -> {
            if (Hooks.DETECT_CONTEXT_LOSS) {
               ContextTrackingFunctionWrapper<T, V> wrapper = new ContextTrackingFunctionWrapper<>(
                  publisher -> (Publisher)transformer.apply(wrap(publisher), ctxView), transformer.toString()
               );
               return wrapper.apply(this);
            } else {
               return (Publisher)transformer.apply(this, ctxView);
            }
         }
      );
   }

   public final Flux<Flux<T>> window(int maxSize) {
      return onAssembly(new FluxWindow<>(this, maxSize, Queues.get(maxSize)));
   }

   public final Flux<Flux<T>> window(int maxSize, int skip) {
      return onAssembly(new FluxWindow<>(this, maxSize, skip, Queues.unbounded(Queues.XS_BUFFER_SIZE), Queues.unbounded(Queues.XS_BUFFER_SIZE)));
   }

   public final Flux<Flux<T>> window(Publisher<?> boundary) {
      return onAssembly(new FluxWindowBoundary<>(this, boundary, Queues.unbounded(Queues.XS_BUFFER_SIZE)));
   }

   public final Flux<Flux<T>> window(Duration windowingTimespan) {
      return this.window(windowingTimespan, Schedulers.parallel());
   }

   public final Flux<Flux<T>> window(Duration windowingTimespan, Duration openWindowEvery) {
      return this.window(windowingTimespan, openWindowEvery, Schedulers.parallel());
   }

   public final Flux<Flux<T>> window(Duration windowingTimespan, Scheduler timer) {
      return this.window(interval(windowingTimespan, timer));
   }

   public final Flux<Flux<T>> window(Duration windowingTimespan, Duration openWindowEvery, Scheduler timer) {
      return openWindowEvery.equals(windowingTimespan)
         ? this.window(windowingTimespan)
         : this.windowWhen(interval(Duration.ZERO, openWindowEvery, timer), aLong -> Mono.delay(windowingTimespan, timer));
   }

   public final Flux<Flux<T>> windowTimeout(int maxSize, Duration maxTime) {
      return this.windowTimeout(maxSize, maxTime, Schedulers.parallel());
   }

   public final Flux<Flux<T>> windowTimeout(int maxSize, Duration maxTime, Scheduler timer) {
      return onAssembly(new FluxWindowTimeout<>(this, maxSize, maxTime.toNanos(), TimeUnit.NANOSECONDS, timer));
   }

   public final Flux<Flux<T>> windowUntil(Predicate<T> boundaryTrigger) {
      return this.windowUntil(boundaryTrigger, false);
   }

   public final Flux<Flux<T>> windowUntil(Predicate<T> boundaryTrigger, boolean cutBefore) {
      return this.windowUntil(boundaryTrigger, cutBefore, Queues.SMALL_BUFFER_SIZE);
   }

   public final Flux<Flux<T>> windowUntil(Predicate<T> boundaryTrigger, boolean cutBefore, int prefetch) {
      return onAssembly(
         new FluxWindowPredicate<>(
            this,
            Queues.unbounded(prefetch),
            Queues.unbounded(prefetch),
            prefetch,
            boundaryTrigger,
            cutBefore ? FluxBufferPredicate.Mode.UNTIL_CUT_BEFORE : FluxBufferPredicate.Mode.UNTIL
         )
      );
   }

   public final <V> Flux<Flux<T>> windowUntilChanged() {
      return this.windowUntilChanged(identityFunction());
   }

   public final <V> Flux<Flux<T>> windowUntilChanged(Function<? super T, ? super V> keySelector) {
      return this.windowUntilChanged((Function<? super T, ? extends V>)keySelector, equalPredicate());
   }

   public final <V> Flux<Flux<T>> windowUntilChanged(Function<? super T, ? extends V> keySelector, BiPredicate<? super V, ? super V> keyComparator) {
      return defer(() -> this.windowUntil(new FluxBufferPredicate.ChangedPredicate(keySelector, keyComparator), true));
   }

   public final Flux<Flux<T>> windowWhile(Predicate<T> inclusionPredicate) {
      return this.windowWhile(inclusionPredicate, Queues.SMALL_BUFFER_SIZE);
   }

   public final Flux<Flux<T>> windowWhile(Predicate<T> inclusionPredicate, int prefetch) {
      return onAssembly(
         new FluxWindowPredicate<>(this, Queues.unbounded(prefetch), Queues.unbounded(prefetch), prefetch, inclusionPredicate, FluxBufferPredicate.Mode.WHILE)
      );
   }

   public final <U, V> Flux<Flux<T>> windowWhen(Publisher<U> bucketOpening, Function<? super U, ? extends Publisher<V>> closeSelector) {
      return onAssembly(new FluxWindowWhen<>(this, bucketOpening, closeSelector, Queues.unbounded(Queues.XS_BUFFER_SIZE)));
   }

   public final <U, R> Flux<R> withLatestFrom(Publisher<? extends U> other, BiFunction<? super T, ? super U, ? extends R> resultSelector) {
      return onAssembly(new FluxWithLatestFrom<>(this, other, resultSelector));
   }

   public final <T2> Flux<Tuple2<T, T2>> zipWith(Publisher<? extends T2> source2) {
      return this.zipWith(source2, tuple2Function());
   }

   public final <T2, V> Flux<V> zipWith(Publisher<? extends T2> source2, BiFunction<? super T, ? super T2, ? extends V> combinator) {
      if (this instanceof FluxZip) {
         FluxZip<T, V> o = (FluxZip)this;
         Flux<V> result = o.zipAdditionalSource(source2, combinator);
         if (result != null) {
            return result;
         }
      }

      return zip(this, source2, combinator);
   }

   public final <T2, V> Flux<V> zipWith(Publisher<? extends T2> source2, int prefetch, BiFunction<? super T, ? super T2, ? extends V> combinator) {
      return zip(objects -> combinator.apply(objects[0], objects[1]), prefetch, this, source2);
   }

   public final <T2> Flux<Tuple2<T, T2>> zipWith(Publisher<? extends T2> source2, int prefetch) {
      return this.zipWith(source2, prefetch, tuple2Function());
   }

   public final <T2> Flux<Tuple2<T, T2>> zipWithIterable(Iterable<? extends T2> iterable) {
      return this.zipWithIterable(iterable, tuple2Function());
   }

   public final <T2, V> Flux<V> zipWithIterable(Iterable<? extends T2> iterable, BiFunction<? super T, ? super T2, ? extends V> zipper) {
      return onAssembly(new FluxZipIterable<>(this, iterable, zipper));
   }

   protected static <T> Flux<T> onAssembly(Flux<T> source) {
      Function<Publisher, Publisher> hook = Hooks.onEachOperatorHook;
      if (hook != null) {
         source = (Flux)hook.apply(source);
      }

      if (Hooks.GLOBAL_TRACE) {
         FluxOnAssembly.AssemblySnapshot stacktrace = new FluxOnAssembly.AssemblySnapshot(null, (Supplier<String>)Traces.callSiteSupplierFactory.get());
         source = (Flux)Hooks.<T, Flux<T>>addAssemblyInfo(source, stacktrace);
      }

      return source;
   }

   protected static <T> ConnectableFlux<T> onAssembly(ConnectableFlux<T> source) {
      Function<Publisher, Publisher> hook = Hooks.onEachOperatorHook;
      if (hook != null) {
         source = (ConnectableFlux)hook.apply(source);
      }

      if (Hooks.GLOBAL_TRACE) {
         FluxOnAssembly.AssemblySnapshot stacktrace = new FluxOnAssembly.AssemblySnapshot(null, (Supplier<String>)Traces.callSiteSupplierFactory.get());
         source = (ConnectableFlux)Hooks.<T, ConnectableFlux<T>>addAssemblyInfo(source, stacktrace);
      }

      return source;
   }

   public String toString() {
      return this.getClass().getSimpleName();
   }

   final <V> Flux<V> flatMap(Function<? super T, ? extends Publisher<? extends V>> mapper, boolean delayError, int concurrency, int prefetch) {
      return onAssembly(new FluxFlatMap<>(this, mapper, delayError, concurrency, Queues.get(concurrency), prefetch, Queues.get(prefetch)));
   }

   final <R> Flux<R> flatMapSequential(Function<? super T, ? extends Publisher<? extends R>> mapper, boolean delayError, int maxConcurrency, int prefetch) {
      return onAssembly(
         new FluxMergeSequential<>(this, mapper, maxConcurrency, prefetch, delayError ? FluxConcatMap.ErrorMode.END : FluxConcatMap.ErrorMode.IMMEDIATE)
      );
   }

   static <T> Flux<T> doOnSignal(
      Flux<T> source,
      @Nullable Consumer<? super Subscription> onSubscribe,
      @Nullable Consumer<? super T> onNext,
      @Nullable Consumer<? super Throwable> onError,
      @Nullable Runnable onComplete,
      @Nullable Runnable onAfterTerminate,
      @Nullable LongConsumer onRequest,
      @Nullable Runnable onCancel
   ) {
      return source instanceof Fuseable
         ? onAssembly(new FluxPeekFuseable<>(source, onSubscribe, onNext, onError, onComplete, onAfterTerminate, onRequest, onCancel))
         : onAssembly(new FluxPeek<>(source, onSubscribe, onNext, onError, onComplete, onAfterTerminate, onRequest, onCancel));
   }

   static <T> Mono<T> wrapToMono(Callable<T> supplier) {
      if (supplier instanceof Fuseable.ScalarCallable) {
         Fuseable.ScalarCallable<T> scalarCallable = (Fuseable.ScalarCallable)supplier;

         T v;
         try {
            v = (T)scalarCallable.call();
         } catch (Exception var4) {
            return new MonoError<>(Exceptions.unwrap(var4));
         }

         return (Mono<T>)(v == null ? MonoEmpty.instance() : new MonoJust<>(v));
      } else {
         return new MonoCallable<>(supplier);
      }
   }

   @SafeVarargs
   static <I> Flux<I> merge(int prefetch, boolean delayError, Publisher<? extends I>... sources) {
      if (sources.length == 0) {
         return empty();
      } else {
         return sources.length == 1
            ? from(sources[0])
            : onAssembly(new FluxMerge<>(sources, delayError, sources.length, Queues.get(sources.length), prefetch, Queues.get(prefetch)));
      }
   }

   @SafeVarargs
   static <I> Flux<I> mergeSequential(int prefetch, boolean delayError, Publisher<? extends I>... sources) {
      if (sources.length == 0) {
         return empty();
      } else {
         return sources.length == 1
            ? from(sources[0])
            : onAssembly(
               new FluxMergeSequential<>(
                  new FluxArray<>(sources),
                  identityFunction(),
                  sources.length,
                  prefetch,
                  delayError ? FluxConcatMap.ErrorMode.END : FluxConcatMap.ErrorMode.IMMEDIATE
               )
            );
      }
   }

   static <T> Flux<T> mergeSequential(Publisher<? extends Publisher<? extends T>> sources, boolean delayError, int maxConcurrency, int prefetch) {
      return onAssembly(
         new FluxMergeSequential<>(
            from(sources), identityFunction(), maxConcurrency, prefetch, delayError ? FluxConcatMap.ErrorMode.END : FluxConcatMap.ErrorMode.IMMEDIATE
         )
      );
   }

   static <I> Flux<I> mergeSequential(Iterable<? extends Publisher<? extends I>> sources, boolean delayError, int maxConcurrency, int prefetch) {
      return onAssembly(
         new FluxMergeSequential<>(
            new FluxIterable<>(sources),
            identityFunction(),
            maxConcurrency,
            prefetch,
            delayError ? FluxConcatMap.ErrorMode.END : FluxConcatMap.ErrorMode.IMMEDIATE
         )
      );
   }

   static BooleanSupplier countingBooleanSupplier(final BooleanSupplier predicate, final long max) {
      return max <= 0L ? predicate : new BooleanSupplier() {
         long n;

         public boolean getAsBoolean() {
            return this.n++ < max && predicate.getAsBoolean();
         }
      };
   }

   static <O> Predicate<O> countingPredicate(final Predicate<O> predicate, final long max) {
      return max == 0L ? predicate : new Predicate<O>() {
         long n;

         public boolean test(O o) {
            return this.n++ < max && predicate.test(o);
         }
      };
   }

   static <O> Supplier<Set<O>> hashSetSupplier() {
      return SET_SUPPLIER;
   }

   static <O> Supplier<List<O>> listSupplier() {
      return LIST_SUPPLIER;
   }

   static <U, V> BiPredicate<U, V> equalPredicate() {
      return OBJECT_EQUAL;
   }

   static <T> Function<T, T> identityFunction() {
      return IDENTITY_FUNCTION;
   }

   static <A, B> BiFunction<A, B, Tuple2<A, B>> tuple2Function() {
      return TUPLE2_BIFUNCTION;
   }

   static <I> Flux<I> wrap(Publisher<? extends I> source) {
      if (source instanceof Flux) {
         return (Flux<I>)source;
      } else if (source instanceof Fuseable.ScalarCallable) {
         try {
            I t = (I)((Fuseable.ScalarCallable)source).call();
            return (Flux<I>)(t != null ? new FluxJust<>(t) : FluxEmpty.instance());
         } catch (Exception var2) {
            return new FluxError<>(Exceptions.unwrap(var2));
         }
      } else if (source instanceof Mono) {
         return (Flux<I>)(source instanceof Fuseable
            ? new FluxSourceMonoFuseable<>((Mono<? extends I>)source)
            : new FluxSourceMono<>((Mono<? extends I>)source));
      } else {
         return (Flux<I>)(source instanceof Fuseable ? new FluxSourceFuseable<>(source) : new FluxSource<>(source));
      }
   }
}
