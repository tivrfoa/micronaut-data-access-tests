package reactor.core.publisher;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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

public abstract class Mono<T> implements CorePublisher<T> {
   static final BiPredicate EQUALS_BIPREDICATE = Object::equals;

   public static <T> Mono<T> create(Consumer<MonoSink<T>> callback) {
      return onAssembly(new MonoCreate<>(callback));
   }

   public static <T> Mono<T> defer(Supplier<? extends Mono<? extends T>> supplier) {
      return onAssembly(new MonoDefer<>(supplier));
   }

   @Deprecated
   public static <T> Mono<T> deferWithContext(Function<Context, ? extends Mono<? extends T>> contextualMonoFactory) {
      return deferContextual(view -> (Mono)contextualMonoFactory.apply(Context.of(view)));
   }

   public static <T> Mono<T> deferContextual(Function<ContextView, ? extends Mono<? extends T>> contextualMonoFactory) {
      return onAssembly(new MonoDeferContextual<>(contextualMonoFactory));
   }

   public static Mono<Long> delay(Duration duration) {
      return delay(duration, Schedulers.parallel());
   }

   public static Mono<Long> delay(Duration duration, Scheduler timer) {
      return onAssembly(new MonoDelay(duration.toNanos(), TimeUnit.NANOSECONDS, timer));
   }

   public static <T> Mono<T> empty() {
      return MonoEmpty.instance();
   }

   public static <T> Mono<T> error(Throwable error) {
      return onAssembly(new MonoError<>(error));
   }

   public static <T> Mono<T> error(Supplier<? extends Throwable> errorSupplier) {
      return onAssembly(new MonoErrorSupplied<>(errorSupplier));
   }

   @SafeVarargs
   @Deprecated
   public static <T> Mono<T> first(Mono<? extends T>... monos) {
      return firstWithSignal(monos);
   }

   @Deprecated
   public static <T> Mono<T> first(Iterable<? extends Mono<? extends T>> monos) {
      return firstWithSignal(monos);
   }

   @SafeVarargs
   public static <T> Mono<T> firstWithSignal(Mono<? extends T>... monos) {
      return onAssembly(new MonoFirstWithSignal<>(monos));
   }

   public static <T> Mono<T> firstWithSignal(Iterable<? extends Mono<? extends T>> monos) {
      return onAssembly(new MonoFirstWithSignal<>(monos));
   }

   public static <T> Mono<T> firstWithValue(Iterable<? extends Mono<? extends T>> monos) {
      return onAssembly(new MonoFirstWithValue<>(monos));
   }

   @SafeVarargs
   public static <T> Mono<T> firstWithValue(Mono<? extends T> first, Mono<? extends T>... others) {
      if (first instanceof MonoFirstWithValue) {
         MonoFirstWithValue<T> a = (MonoFirstWithValue)first;
         Mono<T> result = a.firstValuedAdditionalSources(others);
         if (result != null) {
            return result;
         }
      }

      return onAssembly(new MonoFirstWithValue<>(first, others));
   }

   public static <T> Mono<T> from(Publisher<? extends T> source) {
      if (source instanceof Mono) {
         Mono<T> casted = (Mono)source;
         return casted;
      } else if (!(source instanceof FluxSourceMono) && !(source instanceof FluxSourceMonoFuseable)) {
         return onAssembly(wrap(source, true));
      } else {
         FluxFromMonoOperator<T, T> wrapper = (FluxFromMonoOperator)source;
         return wrapper.source;
      }
   }

   public static <T> Mono<T> fromCallable(Callable<? extends T> supplier) {
      return onAssembly(new MonoCallable<>(supplier));
   }

   public static <T> Mono<T> fromCompletionStage(CompletionStage<? extends T> completionStage) {
      return onAssembly(new MonoCompletionStage<>(completionStage));
   }

   public static <T> Mono<T> fromCompletionStage(Supplier<? extends CompletionStage<? extends T>> stageSupplier) {
      return defer(() -> onAssembly(new MonoCompletionStage<>((CompletionStage<? extends T>)stageSupplier.get())));
   }

   public static <I> Mono<I> fromDirect(Publisher<? extends I> source) {
      if (source instanceof Mono) {
         Mono<I> m = (Mono)source;
         return m;
      } else if (!(source instanceof FluxSourceMono) && !(source instanceof FluxSourceMonoFuseable)) {
         return onAssembly(wrap(source, false));
      } else {
         FluxFromMonoOperator<I, I> wrapper = (FluxFromMonoOperator)source;
         return wrapper.source;
      }
   }

   public static <T> Mono<T> fromFuture(CompletableFuture<? extends T> future) {
      return onAssembly(new MonoCompletionStage<>(future));
   }

   public static <T> Mono<T> fromFuture(Supplier<? extends CompletableFuture<? extends T>> futureSupplier) {
      return defer(() -> onAssembly(new MonoCompletionStage<>((CompletionStage<? extends T>)futureSupplier.get())));
   }

   public static <T> Mono<T> fromRunnable(Runnable runnable) {
      return onAssembly(new MonoRunnable<>(runnable));
   }

   public static <T> Mono<T> fromSupplier(Supplier<? extends T> supplier) {
      return onAssembly(new MonoSupplier<>(supplier));
   }

   public static <T> Mono<T> ignoreElements(Publisher<T> source) {
      return onAssembly(new MonoIgnorePublisher<>(source));
   }

   public static <T> Mono<T> just(T data) {
      return onAssembly(new MonoJust<>(data));
   }

   public static <T> Mono<T> justOrEmpty(@Nullable Optional<? extends T> data) {
      return data != null && data.isPresent() ? just((T)data.get()) : empty();
   }

   public static <T> Mono<T> justOrEmpty(@Nullable T data) {
      return data != null ? just(data) : empty();
   }

   public static <T> Mono<T> never() {
      return MonoNever.instance();
   }

   public static <T> Mono<Boolean> sequenceEqual(Publisher<? extends T> source1, Publisher<? extends T> source2) {
      return sequenceEqual(source1, source2, equalsBiPredicate(), Queues.SMALL_BUFFER_SIZE);
   }

   public static <T> Mono<Boolean> sequenceEqual(Publisher<? extends T> source1, Publisher<? extends T> source2, BiPredicate<? super T, ? super T> isEqual) {
      return sequenceEqual(source1, source2, isEqual, Queues.SMALL_BUFFER_SIZE);
   }

   public static <T> Mono<Boolean> sequenceEqual(
      Publisher<? extends T> source1, Publisher<? extends T> source2, BiPredicate<? super T, ? super T> isEqual, int prefetch
   ) {
      return onAssembly(new MonoSequenceEqual<>(source1, source2, isEqual, prefetch));
   }

   @Deprecated
   public static Mono<Context> subscriberContext() {
      return onAssembly(MonoCurrentContext.INSTANCE);
   }

   public static <T, D> Mono<T> using(
      Callable<? extends D> resourceSupplier,
      Function<? super D, ? extends Mono<? extends T>> sourceSupplier,
      Consumer<? super D> resourceCleanup,
      boolean eager
   ) {
      return onAssembly(new MonoUsing<>(resourceSupplier, sourceSupplier, resourceCleanup, eager));
   }

   public static <T, D> Mono<T> using(
      Callable<? extends D> resourceSupplier, Function<? super D, ? extends Mono<? extends T>> sourceSupplier, Consumer<? super D> resourceCleanup
   ) {
      return using(resourceSupplier, sourceSupplier, resourceCleanup, true);
   }

   public static <T, D> Mono<T> usingWhen(
      Publisher<D> resourceSupplier, Function<? super D, ? extends Mono<? extends T>> resourceClosure, Function<? super D, ? extends Publisher<?>> asyncCleanup
   ) {
      return usingWhen(resourceSupplier, resourceClosure, asyncCleanup, (res, error) -> (Publisher)asyncCleanup.apply(res), asyncCleanup);
   }

   public static <T, D> Mono<T> usingWhen(
      Publisher<D> resourceSupplier,
      Function<? super D, ? extends Mono<? extends T>> resourceClosure,
      Function<? super D, ? extends Publisher<?>> asyncComplete,
      BiFunction<? super D, ? super Throwable, ? extends Publisher<?>> asyncError,
      Function<? super D, ? extends Publisher<?>> asyncCancel
   ) {
      return onAssembly(new MonoUsingWhen<>(resourceSupplier, resourceClosure, asyncComplete, asyncError, asyncCancel));
   }

   public static Mono<Void> when(Publisher<?>... sources) {
      if (sources.length == 0) {
         return empty();
      } else {
         return sources.length == 1 ? empty(sources[0]) : onAssembly(new MonoWhen(false, sources));
      }
   }

   public static Mono<Void> when(Iterable<? extends Publisher<?>> sources) {
      return onAssembly(new MonoWhen(false, sources));
   }

   public static Mono<Void> whenDelayError(Iterable<? extends Publisher<?>> sources) {
      return onAssembly(new MonoWhen(true, sources));
   }

   public static Mono<Void> whenDelayError(Publisher<?>... sources) {
      if (sources.length == 0) {
         return empty();
      } else {
         return sources.length == 1 ? empty(sources[0]) : onAssembly(new MonoWhen(true, sources));
      }
   }

   public static <T1, T2> Mono<Tuple2<T1, T2>> zip(Mono<? extends T1> p1, Mono<? extends T2> p2) {
      return zip(p1, p2, Flux.tuple2Function());
   }

   public static <T1, T2, O> Mono<O> zip(Mono<? extends T1> p1, Mono<? extends T2> p2, BiFunction<? super T1, ? super T2, ? extends O> combinator) {
      return onAssembly(new MonoZip<>(false, p1, p2, combinator));
   }

   public static <T1, T2, T3> Mono<Tuple3<T1, T2, T3>> zip(Mono<? extends T1> p1, Mono<? extends T2> p2, Mono<? extends T3> p3) {
      return onAssembly(new MonoZip<>(false, a -> Tuples.fromArray(a), p1, p2, p3));
   }

   public static <T1, T2, T3, T4> Mono<Tuple4<T1, T2, T3, T4>> zip(Mono<? extends T1> p1, Mono<? extends T2> p2, Mono<? extends T3> p3, Mono<? extends T4> p4) {
      return onAssembly(new MonoZip<>(false, a -> Tuples.fromArray(a), p1, p2, p3, p4));
   }

   public static <T1, T2, T3, T4, T5> Mono<Tuple5<T1, T2, T3, T4, T5>> zip(
      Mono<? extends T1> p1, Mono<? extends T2> p2, Mono<? extends T3> p3, Mono<? extends T4> p4, Mono<? extends T5> p5
   ) {
      return onAssembly(new MonoZip<>(false, a -> Tuples.fromArray(a), p1, p2, p3, p4, p5));
   }

   public static <T1, T2, T3, T4, T5, T6> Mono<Tuple6<T1, T2, T3, T4, T5, T6>> zip(
      Mono<? extends T1> p1, Mono<? extends T2> p2, Mono<? extends T3> p3, Mono<? extends T4> p4, Mono<? extends T5> p5, Mono<? extends T6> p6
   ) {
      return onAssembly(new MonoZip<>(false, a -> Tuples.fromArray(a), p1, p2, p3, p4, p5, p6));
   }

   public static <T1, T2, T3, T4, T5, T6, T7> Mono<Tuple7<T1, T2, T3, T4, T5, T6, T7>> zip(
      Mono<? extends T1> p1,
      Mono<? extends T2> p2,
      Mono<? extends T3> p3,
      Mono<? extends T4> p4,
      Mono<? extends T5> p5,
      Mono<? extends T6> p6,
      Mono<? extends T7> p7
   ) {
      return onAssembly(new MonoZip<>(false, a -> Tuples.fromArray(a), p1, p2, p3, p4, p5, p6, p7));
   }

   public static <T1, T2, T3, T4, T5, T6, T7, T8> Mono<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> zip(
      Mono<? extends T1> p1,
      Mono<? extends T2> p2,
      Mono<? extends T3> p3,
      Mono<? extends T4> p4,
      Mono<? extends T5> p5,
      Mono<? extends T6> p6,
      Mono<? extends T7> p7,
      Mono<? extends T8> p8
   ) {
      return onAssembly(new MonoZip<>(false, a -> Tuples.fromArray(a), p1, p2, p3, p4, p5, p6, p7, p8));
   }

   public static <R> Mono<R> zip(Iterable<? extends Mono<?>> monos, Function<? super Object[], ? extends R> combinator) {
      return onAssembly(new MonoZip<>(false, combinator, monos));
   }

   public static <R> Mono<R> zip(Function<? super Object[], ? extends R> combinator, Mono<?>... monos) {
      if (monos.length == 0) {
         return empty();
      } else {
         return monos.length == 1 ? monos[0].map(d -> combinator.apply(new Object[]{d})) : onAssembly(new MonoZip<>(false, combinator, monos));
      }
   }

   public static <T1, T2> Mono<Tuple2<T1, T2>> zipDelayError(Mono<? extends T1> p1, Mono<? extends T2> p2) {
      return onAssembly(new MonoZip<>(true, a -> Tuples.fromArray(a), p1, p2));
   }

   public static <T1, T2, T3> Mono<Tuple3<T1, T2, T3>> zipDelayError(Mono<? extends T1> p1, Mono<? extends T2> p2, Mono<? extends T3> p3) {
      return onAssembly(new MonoZip<>(true, a -> Tuples.fromArray(a), p1, p2, p3));
   }

   public static <T1, T2, T3, T4> Mono<Tuple4<T1, T2, T3, T4>> zipDelayError(
      Mono<? extends T1> p1, Mono<? extends T2> p2, Mono<? extends T3> p3, Mono<? extends T4> p4
   ) {
      return onAssembly(new MonoZip<>(true, a -> Tuples.fromArray(a), p1, p2, p3, p4));
   }

   public static <T1, T2, T3, T4, T5> Mono<Tuple5<T1, T2, T3, T4, T5>> zipDelayError(
      Mono<? extends T1> p1, Mono<? extends T2> p2, Mono<? extends T3> p3, Mono<? extends T4> p4, Mono<? extends T5> p5
   ) {
      return onAssembly(new MonoZip<>(true, a -> Tuples.fromArray(a), p1, p2, p3, p4, p5));
   }

   public static <T1, T2, T3, T4, T5, T6> Mono<Tuple6<T1, T2, T3, T4, T5, T6>> zipDelayError(
      Mono<? extends T1> p1, Mono<? extends T2> p2, Mono<? extends T3> p3, Mono<? extends T4> p4, Mono<? extends T5> p5, Mono<? extends T6> p6
   ) {
      return onAssembly(new MonoZip<>(true, a -> Tuples.fromArray(a), p1, p2, p3, p4, p5, p6));
   }

   public static <T1, T2, T3, T4, T5, T6, T7> Mono<Tuple7<T1, T2, T3, T4, T5, T6, T7>> zipDelayError(
      Mono<? extends T1> p1,
      Mono<? extends T2> p2,
      Mono<? extends T3> p3,
      Mono<? extends T4> p4,
      Mono<? extends T5> p5,
      Mono<? extends T6> p6,
      Mono<? extends T7> p7
   ) {
      return onAssembly(new MonoZip<>(true, a -> Tuples.fromArray(a), p1, p2, p3, p4, p5, p6, p7));
   }

   public static <T1, T2, T3, T4, T5, T6, T7, T8> Mono<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> zipDelayError(
      Mono<? extends T1> p1,
      Mono<? extends T2> p2,
      Mono<? extends T3> p3,
      Mono<? extends T4> p4,
      Mono<? extends T5> p5,
      Mono<? extends T6> p6,
      Mono<? extends T7> p7,
      Mono<? extends T8> p8
   ) {
      return onAssembly(new MonoZip<>(true, a -> Tuples.fromArray(a), p1, p2, p3, p4, p5, p6, p7, p8));
   }

   public static <R> Mono<R> zipDelayError(Iterable<? extends Mono<?>> monos, Function<? super Object[], ? extends R> combinator) {
      return onAssembly(new MonoZip<>(true, combinator, monos));
   }

   public static <R> Mono<R> zipDelayError(Function<? super Object[], ? extends R> combinator, Mono<?>... monos) {
      if (monos.length == 0) {
         return empty();
      } else {
         return monos.length == 1 ? monos[0].map(d -> combinator.apply(new Object[]{d})) : onAssembly(new MonoZip<>(true, combinator, monos));
      }
   }

   public final <P> P as(Function<? super Mono<T>, P> transformer) {
      return (P)transformer.apply(this);
   }

   public final Mono<Void> and(Publisher<?> other) {
      if (this instanceof MonoWhen) {
         MonoWhen o = (MonoWhen)this;
         Mono<Void> result = o.whenAdditionalSource(other);
         if (result != null) {
            return result;
         }
      }

      return when(this, other);
   }

   @Nullable
   public T block() {
      BlockingMonoSubscriber<T> subscriber = new BlockingMonoSubscriber<>();
      this.subscribe(subscriber);
      return subscriber.blockingGet();
   }

   @Nullable
   public T block(Duration timeout) {
      BlockingMonoSubscriber<T> subscriber = new BlockingMonoSubscriber<>();
      this.subscribe(subscriber);
      return subscriber.blockingGet(timeout.toNanos(), TimeUnit.NANOSECONDS);
   }

   public Optional<T> blockOptional() {
      BlockingOptionalMonoSubscriber<T> subscriber = new BlockingOptionalMonoSubscriber<>();
      this.subscribe(subscriber);
      return subscriber.blockingGet();
   }

   public Optional<T> blockOptional(Duration timeout) {
      BlockingOptionalMonoSubscriber<T> subscriber = new BlockingOptionalMonoSubscriber<>();
      this.subscribe(subscriber);
      return subscriber.blockingGet(timeout.toNanos(), TimeUnit.NANOSECONDS);
   }

   public final <E> Mono<E> cast(Class<E> clazz) {
      Objects.requireNonNull(clazz, "clazz");
      return this.map(clazz::cast);
   }

   public final Mono<T> cache() {
      return onAssembly(new MonoCacheTime<>(this));
   }

   public final Mono<T> cache(Duration ttl) {
      return this.cache(ttl, Schedulers.parallel());
   }

   public final Mono<T> cache(Duration ttl, Scheduler timer) {
      return onAssembly(new MonoCacheTime<>(this, ttl, timer));
   }

   public final Mono<T> cache(Function<? super T, Duration> ttlForValue, Function<Throwable, Duration> ttlForError, Supplier<Duration> ttlForEmpty) {
      return this.cache(ttlForValue, ttlForError, ttlForEmpty, Schedulers.parallel());
   }

   public final Mono<T> cache(
      Function<? super T, Duration> ttlForValue, Function<Throwable, Duration> ttlForError, Supplier<Duration> ttlForEmpty, Scheduler timer
   ) {
      return onAssembly(new MonoCacheTime<>(this, ttlForValue, ttlForError, ttlForEmpty, timer));
   }

   public final Mono<T> cacheInvalidateIf(Predicate<? super T> invalidationPredicate) {
      return onAssembly(new MonoCacheInvalidateIf<>(this, invalidationPredicate));
   }

   public final Mono<T> cacheInvalidateWhen(Function<? super T, Mono<Void>> invalidationTriggerGenerator) {
      return onAssembly(new MonoCacheInvalidateWhen<>(this, invalidationTriggerGenerator, null));
   }

   public final Mono<T> cacheInvalidateWhen(Function<? super T, Mono<Void>> invalidationTriggerGenerator, Consumer<? super T> onInvalidate) {
      return onAssembly(new MonoCacheInvalidateWhen<>(this, invalidationTriggerGenerator, onInvalidate));
   }

   public final Mono<T> cancelOn(Scheduler scheduler) {
      return onAssembly(new MonoCancelOn<>(this, scheduler));
   }

   public final Mono<T> checkpoint() {
      return this.checkpoint(null, true);
   }

   public final Mono<T> checkpoint(String description) {
      return this.checkpoint((String)Objects.requireNonNull(description), false);
   }

   public final Mono<T> checkpoint(@Nullable String description, boolean forceStackTrace) {
      FluxOnAssembly.AssemblySnapshot stacktrace;
      if (!forceStackTrace) {
         stacktrace = new FluxOnAssembly.CheckpointLightSnapshot(description);
      } else {
         stacktrace = new FluxOnAssembly.CheckpointHeavySnapshot(description, (Supplier<String>)Traces.callSiteSupplierFactory.get());
      }

      return new MonoOnAssembly<>(this, stacktrace);
   }

   public final Flux<T> concatWith(Publisher<? extends T> other) {
      return Flux.concat(this, other);
   }

   public final Mono<T> contextWrite(ContextView contextToAppend) {
      return this.contextWrite(c -> c.putAll(contextToAppend));
   }

   public final Mono<T> contextWrite(Function<Context, Context> contextModifier) {
      return onAssembly(new MonoContextWrite<>(this, contextModifier));
   }

   public final Mono<T> defaultIfEmpty(T defaultV) {
      if (this instanceof Fuseable.ScalarCallable) {
         try {
            T v = this.block();
            if (v == null) {
               return just(defaultV);
            }
         } catch (Throwable var3) {
         }

         return this;
      } else {
         return onAssembly(new MonoDefaultIfEmpty<>(this, defaultV));
      }
   }

   public final Mono<T> delayElement(Duration delay) {
      return this.delayElement(delay, Schedulers.parallel());
   }

   public final Mono<T> delayElement(Duration delay, Scheduler timer) {
      return onAssembly(new MonoDelayElement<>(this, delay.toNanos(), TimeUnit.NANOSECONDS, timer));
   }

   public final Mono<T> delayUntil(Function<? super T, ? extends Publisher<?>> triggerProvider) {
      Objects.requireNonNull(triggerProvider, "triggerProvider required");
      return (Mono<T>)(this instanceof MonoDelayUntil
         ? ((MonoDelayUntil)this).copyWithNewTriggerGenerator(false, triggerProvider)
         : onAssembly(new MonoDelayUntil<>(this, triggerProvider)));
   }

   public final Mono<T> delaySubscription(Duration delay) {
      return this.delaySubscription(delay, Schedulers.parallel());
   }

   public final Mono<T> delaySubscription(Duration delay, Scheduler timer) {
      return this.delaySubscription(delay(delay, timer));
   }

   public final <U> Mono<T> delaySubscription(Publisher<U> subscriptionDelay) {
      return onAssembly(new MonoDelaySubscription<>(this, subscriptionDelay));
   }

   public final <X> Mono<X> dematerialize() {
      return onAssembly(new MonoDematerialize<>(this));
   }

   @Deprecated
   public final Mono<T> doAfterSuccessOrError(BiConsumer<? super T, Throwable> afterSuccessOrError) {
      return doOnTerminalSignal(this, null, null, afterSuccessOrError);
   }

   public final Mono<T> doAfterTerminate(Runnable afterTerminate) {
      Objects.requireNonNull(afterTerminate, "afterTerminate");
      return onAssembly(new MonoPeekTerminal<>(this, null, null, (s, e) -> afterTerminate.run()));
   }

   public final Mono<T> doFirst(Runnable onFirst) {
      Objects.requireNonNull(onFirst, "onFirst");
      return this instanceof Fuseable ? onAssembly(new MonoDoFirstFuseable<>(this, onFirst)) : onAssembly(new MonoDoFirst<>(this, onFirst));
   }

   public final Mono<T> doFinally(Consumer<SignalType> onFinally) {
      Objects.requireNonNull(onFinally, "onFinally");
      return this instanceof Fuseable ? onAssembly(new MonoDoFinallyFuseable<>(this, onFinally)) : onAssembly(new MonoDoFinally<>(this, onFinally));
   }

   public final Mono<T> doOnCancel(Runnable onCancel) {
      Objects.requireNonNull(onCancel, "onCancel");
      return doOnSignal(this, null, null, null, onCancel);
   }

   public final <R> Mono<T> doOnDiscard(Class<R> type, Consumer<? super R> discardHook) {
      return this.subscriberContext(Operators.discardLocalAdapter(type, discardHook));
   }

   public final Mono<T> doOnNext(Consumer<? super T> onNext) {
      Objects.requireNonNull(onNext, "onNext");
      return doOnSignal(this, null, onNext, null, null);
   }

   public final Mono<T> doOnSuccess(Consumer<? super T> onSuccess) {
      Objects.requireNonNull(onSuccess, "onSuccess");
      return doOnTerminalSignal(this, onSuccess, null, null);
   }

   public final Mono<T> doOnEach(Consumer<? super Signal<T>> signalConsumer) {
      Objects.requireNonNull(signalConsumer, "signalConsumer");
      return this instanceof Fuseable ? onAssembly(new MonoDoOnEachFuseable<>(this, signalConsumer)) : onAssembly(new MonoDoOnEach<>(this, signalConsumer));
   }

   public final Mono<T> doOnError(Consumer<? super Throwable> onError) {
      Objects.requireNonNull(onError, "onError");
      return doOnTerminalSignal(this, null, onError, null);
   }

   public final <E extends Throwable> Mono<T> doOnError(Class<E> exceptionType, Consumer<? super E> onError) {
      Objects.requireNonNull(exceptionType, "type");
      Objects.requireNonNull(onError, "onError");
      return doOnTerminalSignal(this, null, error -> {
         if (exceptionType.isInstance(error)) {
            onError.accept(exceptionType.cast(error));
         }

      }, null);
   }

   public final Mono<T> doOnError(Predicate<? super Throwable> predicate, Consumer<? super Throwable> onError) {
      Objects.requireNonNull(predicate, "predicate");
      Objects.requireNonNull(onError, "onError");
      return doOnTerminalSignal(this, null, error -> {
         if (predicate.test(error)) {
            onError.accept(error);
         }

      }, null);
   }

   public final Mono<T> doOnRequest(LongConsumer consumer) {
      Objects.requireNonNull(consumer, "consumer");
      return doOnSignal(this, null, null, consumer, null);
   }

   public final Mono<T> doOnSubscribe(Consumer<? super Subscription> onSubscribe) {
      Objects.requireNonNull(onSubscribe, "onSubscribe");
      return doOnSignal(this, onSubscribe, null, null, null);
   }

   @Deprecated
   public final Mono<T> doOnSuccessOrError(BiConsumer<? super T, Throwable> onSuccessOrError) {
      Objects.requireNonNull(onSuccessOrError, "onSuccessOrError");
      return doOnTerminalSignal(this, v -> onSuccessOrError.accept(v, null), e -> onSuccessOrError.accept(null, e), null);
   }

   public final Mono<T> doOnTerminate(Runnable onTerminate) {
      Objects.requireNonNull(onTerminate, "onTerminate");
      return doOnTerminalSignal(this, ignoreValue -> onTerminate.run(), ignoreError -> onTerminate.run(), null);
   }

   public final Mono<Tuple2<Long, T>> elapsed() {
      return this.elapsed(Schedulers.parallel());
   }

   public final Mono<Tuple2<Long, T>> elapsed(Scheduler scheduler) {
      Objects.requireNonNull(scheduler, "scheduler");
      return onAssembly(new MonoElapsed<>(this, scheduler));
   }

   public final Flux<T> expandDeep(Function<? super T, ? extends Publisher<? extends T>> expander, int capacityHint) {
      return Flux.onAssembly(new MonoExpand<>(this, expander, false, capacityHint));
   }

   public final Flux<T> expandDeep(Function<? super T, ? extends Publisher<? extends T>> expander) {
      return this.expandDeep(expander, Queues.SMALL_BUFFER_SIZE);
   }

   public final Flux<T> expand(Function<? super T, ? extends Publisher<? extends T>> expander, int capacityHint) {
      return Flux.onAssembly(new MonoExpand<>(this, expander, true, capacityHint));
   }

   public final Flux<T> expand(Function<? super T, ? extends Publisher<? extends T>> expander) {
      return this.expand(expander, Queues.SMALL_BUFFER_SIZE);
   }

   public final Mono<T> filter(Predicate<? super T> tester) {
      return this instanceof Fuseable ? onAssembly(new MonoFilterFuseable<>(this, tester)) : onAssembly(new MonoFilter<>(this, tester));
   }

   public final Mono<T> filterWhen(Function<? super T, ? extends Publisher<Boolean>> asyncPredicate) {
      return onAssembly(new MonoFilterWhen<>(this, asyncPredicate));
   }

   public final <R> Mono<R> flatMap(Function<? super T, ? extends Mono<? extends R>> transformer) {
      return onAssembly(new MonoFlatMap<>(this, transformer));
   }

   public final <R> Flux<R> flatMapMany(Function<? super T, ? extends Publisher<? extends R>> mapper) {
      return Flux.onAssembly(new MonoFlatMapMany<>(this, mapper));
   }

   public final <R> Flux<R> flatMapMany(
      Function<? super T, ? extends Publisher<? extends R>> mapperOnNext,
      Function<? super Throwable, ? extends Publisher<? extends R>> mapperOnError,
      Supplier<? extends Publisher<? extends R>> mapperOnComplete
   ) {
      return this.flux().flatMap(mapperOnNext, mapperOnError, mapperOnComplete);
   }

   public final <R> Flux<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
      return Flux.onAssembly(new MonoFlattenIterable<>(this, mapper, Integer.MAX_VALUE, Queues.one()));
   }

   public final Flux<T> flux() {
      if (this instanceof Callable && !(this instanceof Fuseable.ScalarCallable)) {
         Callable<T> thiz = (Callable)this;
         return Flux.onAssembly(new FluxCallable<>(thiz));
      } else {
         return Flux.from(this);
      }
   }

   public final Mono<Boolean> hasElement() {
      return onAssembly(new MonoHasElement<>(this));
   }

   public final <R> Mono<R> handle(BiConsumer<? super T, SynchronousSink<R>> handler) {
      return this instanceof Fuseable ? onAssembly(new MonoHandleFuseable<>(this, handler)) : onAssembly(new MonoHandle<>(this, handler));
   }

   public final Mono<T> hide() {
      return onAssembly(new MonoHide<>(this));
   }

   public final Mono<T> ignoreElement() {
      return onAssembly(new MonoIgnoreElement<>(this));
   }

   public final Mono<T> log() {
      return this.log(null, Level.INFO);
   }

   public final Mono<T> log(@Nullable String category) {
      return this.log(category, Level.INFO);
   }

   public final Mono<T> log(@Nullable String category, Level level, SignalType... options) {
      return this.log(category, level, false, options);
   }

   public final Mono<T> log(@Nullable String category, Level level, boolean showOperatorLine, SignalType... options) {
      SignalLogger<T> log = new SignalLogger<>(this, category, level, showOperatorLine, options);
      return this instanceof Fuseable ? onAssembly(new MonoLogFuseable<>(this, log)) : onAssembly(new MonoLog<>(this, log));
   }

   public final Mono<T> log(Logger logger) {
      return this.log(logger, Level.INFO, false);
   }

   public final Mono<T> log(Logger logger, Level level, boolean showOperatorLine, SignalType... options) {
      SignalLogger<T> log = new SignalLogger<>(this, "IGNORED", level, showOperatorLine, s -> logger, options);
      return this instanceof Fuseable ? onAssembly(new MonoLogFuseable<>(this, log)) : onAssembly(new MonoLog<>(this, log));
   }

   public final <R> Mono<R> map(Function<? super T, ? extends R> mapper) {
      return this instanceof Fuseable ? onAssembly(new MonoMapFuseable<>(this, mapper)) : onAssembly(new MonoMap<>(this, mapper));
   }

   public final <R> Mono<R> mapNotNull(Function<? super T, ? extends R> mapper) {
      return this.handle((t, sink) -> {
         R r = (R)mapper.apply(t);
         if (r != null) {
            sink.next(r);
         }

      });
   }

   public final Mono<Signal<T>> materialize() {
      return onAssembly(new MonoMaterialize<>(this));
   }

   public final Flux<T> mergeWith(Publisher<? extends T> other) {
      return Flux.merge(this, other);
   }

   public final Mono<T> metrics() {
      if (!Metrics.isInstrumentationAvailable()) {
         return this;
      } else {
         return this instanceof Fuseable ? onAssembly(new MonoMetricsFuseable<>(this)) : onAssembly(new MonoMetrics<>(this));
      }
   }

   public final Mono<T> name(String name) {
      return MonoName.createOrAppend(this, name);
   }

   public final Mono<T> or(Mono<? extends T> other) {
      if (this instanceof MonoFirstWithSignal) {
         MonoFirstWithSignal<T> a = (MonoFirstWithSignal)this;
         Mono<T> result = a.orAdditionalSource(other);
         if (result != null) {
            return result;
         }
      }

      return firstWithSignal(this, other);
   }

   public final <U> Mono<U> ofType(Class<U> clazz) {
      Objects.requireNonNull(clazz, "clazz");
      return this.filter(o -> clazz.isAssignableFrom(o.getClass())).cast(clazz);
   }

   public final Mono<T> onErrorContinue(BiConsumer<Throwable, Object> errorConsumer) {
      return this.subscriberContext(Context.of("reactor.onNextError.localStrategy", OnNextFailureStrategy.resume(errorConsumer)));
   }

   public final <E extends Throwable> Mono<T> onErrorContinue(Class<E> type, BiConsumer<Throwable, Object> errorConsumer) {
      return this.onErrorContinue(type::isInstance, errorConsumer);
   }

   public final <E extends Throwable> Mono<T> onErrorContinue(Predicate<E> errorPredicate, BiConsumer<Throwable, Object> errorConsumer) {
      return this.subscriberContext(Context.of("reactor.onNextError.localStrategy", OnNextFailureStrategy.resumeIf(errorPredicate, errorConsumer)));
   }

   public final Mono<T> onErrorStop() {
      return this.subscriberContext(Context.of("reactor.onNextError.localStrategy", OnNextFailureStrategy.stop()));
   }

   public final Mono<T> onErrorMap(Predicate<? super Throwable> predicate, Function<? super Throwable, ? extends Throwable> mapper) {
      return this.onErrorResume(predicate, e -> error((Throwable)mapper.apply(e)));
   }

   public final Mono<T> onErrorMap(Function<? super Throwable, ? extends Throwable> mapper) {
      return this.onErrorResume(e -> error((Throwable)mapper.apply(e)));
   }

   public final <E extends Throwable> Mono<T> onErrorMap(Class<E> type, Function<? super E, ? extends Throwable> mapper) {
      return this.onErrorMap(type::isInstance, mapper);
   }

   public final Mono<T> onErrorResume(Function<? super Throwable, ? extends Mono<? extends T>> fallback) {
      return onAssembly(new MonoOnErrorResume<>(this, fallback));
   }

   public final <E extends Throwable> Mono<T> onErrorResume(Class<E> type, Function<? super E, ? extends Mono<? extends T>> fallback) {
      Objects.requireNonNull(type, "type");
      return this.onErrorResume(type::isInstance, fallback);
   }

   public final Mono<T> onErrorResume(Predicate<? super Throwable> predicate, Function<? super Throwable, ? extends Mono<? extends T>> fallback) {
      Objects.requireNonNull(predicate, "predicate");
      return this.onErrorResume(e -> predicate.test(e) ? (Mono)fallback.apply(e) : error(e));
   }

   public final Mono<T> onErrorReturn(T fallback) {
      return this.onErrorResume(throwable -> just(fallback));
   }

   public final <E extends Throwable> Mono<T> onErrorReturn(Class<E> type, T fallbackValue) {
      return this.onErrorResume(type, throwable -> just(fallbackValue));
   }

   public final Mono<T> onErrorReturn(Predicate<? super Throwable> predicate, T fallbackValue) {
      return this.onErrorResume(predicate, throwable -> just(fallbackValue));
   }

   public final Mono<T> onTerminateDetach() {
      return new MonoDetach<>(this);
   }

   public final <R> Mono<R> publish(Function<? super Mono<T>, ? extends Mono<? extends R>> transform) {
      return onAssembly(new MonoPublishMulticast<>(this, transform));
   }

   public final Mono<T> publishOn(Scheduler scheduler) {
      if (!(this instanceof Callable)) {
         return onAssembly(new MonoPublishOn<>(this, scheduler));
      } else {
         if (this instanceof Fuseable.ScalarCallable) {
            try {
               T value = this.block();
               return onAssembly(new MonoSubscribeOnValue<>(value, scheduler));
            } catch (Throwable var3) {
            }
         }

         Callable<T> c = (Callable)this;
         return onAssembly(new MonoSubscribeOnCallable<>(c, scheduler));
      }
   }

   public final Flux<T> repeat() {
      return this.repeat(Flux.ALWAYS_BOOLEAN_SUPPLIER);
   }

   public final Flux<T> repeat(BooleanSupplier predicate) {
      return Flux.onAssembly(new MonoRepeatPredicate<>(this, predicate));
   }

   public final Flux<T> repeat(long numRepeat) {
      return numRepeat == 0L ? this.flux() : Flux.onAssembly(new MonoRepeat<>(this, numRepeat));
   }

   public final Flux<T> repeat(long numRepeat, BooleanSupplier predicate) {
      if (numRepeat < 0L) {
         throw new IllegalArgumentException("numRepeat >= 0 required");
      } else {
         return numRepeat == 0L ? this.flux() : Flux.defer(() -> this.repeat(Flux.countingBooleanSupplier(predicate, numRepeat)));
      }
   }

   public final Flux<T> repeatWhen(Function<Flux<Long>, ? extends Publisher<?>> repeatFactory) {
      return Flux.onAssembly(new MonoRepeatWhen<>(this, repeatFactory));
   }

   public final Mono<T> repeatWhenEmpty(Function<Flux<Long>, ? extends Publisher<?>> repeatFactory) {
      return this.repeatWhenEmpty(Integer.MAX_VALUE, repeatFactory);
   }

   public final Mono<T> repeatWhenEmpty(int maxRepeat, Function<Flux<Long>, ? extends Publisher<?>> repeatFactory) {
      return defer(
         () -> this.repeatWhen(
                  o -> maxRepeat == Integer.MAX_VALUE
                        ? (Publisher)repeatFactory.apply(o.index().map(Tuple2::getT1))
                        : (Publisher)repeatFactory.apply(
                           o.index()
                              .<T>map(Tuple2::getT1)
                              .take((long)maxRepeat)
                              .concatWith(Flux.error((Supplier<? extends Throwable>)(() -> new IllegalStateException("Exceeded maximum number of repeats"))))
                        )
               )
               .next()
      );
   }

   public final Mono<T> retry() {
      return this.retry(Long.MAX_VALUE);
   }

   public final Mono<T> retry(long numRetries) {
      return onAssembly(new MonoRetry<>(this, numRetries));
   }

   public final Mono<T> retryWhen(Retry retrySpec) {
      return onAssembly(new MonoRetryWhen<>(this, retrySpec));
   }

   public final Mono<T> share() {
      if (this instanceof Fuseable.ScalarCallable) {
         return this;
      } else {
         return (Mono<T>)(this instanceof NextProcessor && ((NextProcessor)this).isRefCounted ? this : new NextProcessor<>(this, true));
      }
   }

   public final Mono<T> single() {
      if (this instanceof Callable) {
         if (this instanceof Fuseable.ScalarCallable) {
            Fuseable.ScalarCallable<T> scalarCallable = (Fuseable.ScalarCallable)this;

            T v;
            try {
               v = (T)scalarCallable.call();
            } catch (Exception var4) {
               return error(Exceptions.unwrap(var4));
            }

            return v == null ? error(new NoSuchElementException("Source was a (constant) empty")) : just(v);
         } else {
            Callable<T> thiz = (Callable)this;
            return onAssembly(new MonoSingleCallable<>(thiz));
         }
      } else {
         return onAssembly(new MonoSingleMono<>(this));
      }
   }

   public final Disposable subscribe() {
      if (this instanceof NextProcessor) {
         NextProcessor<T> s = (NextProcessor)this;
         if (s.source != null && !s.isRefCounted) {
            s.subscribe(new LambdaMonoSubscriber<>(null, null, null, null, null));
            s.connect();
            return s;
         }
      }

      return this.subscribeWith(new LambdaMonoSubscriber(null, null, null, null, null));
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

   public final Disposable subscribe(
      @Nullable Consumer<? super T> consumer,
      @Nullable Consumer<? super Throwable> errorConsumer,
      @Nullable Runnable completeConsumer,
      @Nullable Consumer<? super Subscription> subscriptionConsumer
   ) {
      return this.subscribeWith(new LambdaMonoSubscriber(consumer, errorConsumer, completeConsumer, subscriptionConsumer, null));
   }

   public final Disposable subscribe(
      @Nullable Consumer<? super T> consumer,
      @Nullable Consumer<? super Throwable> errorConsumer,
      @Nullable Runnable completeConsumer,
      @Nullable Context initialContext
   ) {
      return this.subscribeWith(new LambdaMonoSubscriber(consumer, errorConsumer, completeConsumer, null, initialContext));
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
   public final Mono<T> subscriberContext(Context mergeContext) {
      return this.subscriberContext(c -> c.putAll(mergeContext.readOnly()));
   }

   @Deprecated
   public final Mono<T> subscriberContext(Function<Context, Context> doOnContext) {
      return new MonoContextWrite<>(this, doOnContext);
   }

   public final Mono<T> subscribeOn(Scheduler scheduler) {
      if (!(this instanceof Callable)) {
         return onAssembly(new MonoSubscribeOn<>(this, scheduler));
      } else {
         if (this instanceof Fuseable.ScalarCallable) {
            try {
               T value = this.block();
               return onAssembly(new MonoSubscribeOnValue<>(value, scheduler));
            } catch (Throwable var3) {
            }
         }

         Callable<T> c = (Callable)this;
         return onAssembly(new MonoSubscribeOnCallable<>(c, scheduler));
      }
   }

   public final <E extends Subscriber<? super T>> E subscribeWith(E subscriber) {
      this.subscribe(subscriber);
      return subscriber;
   }

   public final Mono<T> switchIfEmpty(Mono<? extends T> alternate) {
      return onAssembly(new MonoSwitchIfEmpty<>(this, alternate));
   }

   public final Mono<T> tag(String key, String value) {
      return MonoName.createOrAppend(this, key, value);
   }

   public final Mono<T> take(Duration duration) {
      return this.take(duration, Schedulers.parallel());
   }

   public final Mono<T> take(Duration duration, Scheduler timer) {
      return this.takeUntilOther(delay(duration, timer));
   }

   public final Mono<T> takeUntilOther(Publisher<?> other) {
      return onAssembly(new MonoTakeUntilOther<>(this, other));
   }

   public final Mono<Void> then() {
      return empty(this);
   }

   public final <V> Mono<V> then(Mono<V> other) {
      if (this instanceof MonoIgnoreThen) {
         MonoIgnoreThen<T> a = (MonoIgnoreThen)this;
         return a.shift(other);
      } else {
         return onAssembly(new MonoIgnoreThen<>(new Publisher[]{this}, other));
      }
   }

   public final <V> Mono<V> thenReturn(V value) {
      return this.then(just(value));
   }

   public final Mono<Void> thenEmpty(Publisher<Void> other) {
      return this.then(fromDirect(other));
   }

   public final <V> Flux<V> thenMany(Publisher<V> other) {
      Flux<V> concat = Flux.concat(this.ignoreElement(), other);
      return Flux.onAssembly(concat);
   }

   public final Mono<Timed<T>> timed() {
      return this.timed(Schedulers.parallel());
   }

   public final Mono<Timed<T>> timed(Scheduler clock) {
      return onAssembly(new MonoTimed<>(this, clock));
   }

   public final Mono<T> timeout(Duration timeout) {
      return this.timeout(timeout, Schedulers.parallel());
   }

   public final Mono<T> timeout(Duration timeout, Mono<? extends T> fallback) {
      return this.timeout(timeout, fallback, Schedulers.parallel());
   }

   public final Mono<T> timeout(Duration timeout, Scheduler timer) {
      return this.timeout(timeout, null, timer);
   }

   public final Mono<T> timeout(Duration timeout, @Nullable Mono<? extends T> fallback, Scheduler timer) {
      Mono<Long> _timer = delay(timeout, timer).onErrorReturn((T)0L);
      return fallback == null ? onAssembly(new MonoTimeout<>(this, _timer, timeout.toMillis() + "ms")) : onAssembly(new MonoTimeout<>(this, _timer, fallback));
   }

   public final <U> Mono<T> timeout(Publisher<U> firstTimeout) {
      return onAssembly(new MonoTimeout<>(this, firstTimeout, "first signal from a Publisher"));
   }

   public final <U> Mono<T> timeout(Publisher<U> firstTimeout, Mono<? extends T> fallback) {
      return onAssembly(new MonoTimeout<>(this, firstTimeout, fallback));
   }

   public final Mono<Tuple2<Long, T>> timestamp() {
      return this.timestamp(Schedulers.parallel());
   }

   public final Mono<Tuple2<Long, T>> timestamp(Scheduler scheduler) {
      Objects.requireNonNull(scheduler, "scheduler");
      return this.map(d -> Tuples.of(scheduler.now(TimeUnit.MILLISECONDS), d));
   }

   public final CompletableFuture<T> toFuture() {
      return this.subscribeWith(new MonoToCompletableFuture<>(false));
   }

   @Deprecated
   public final MonoProcessor<T> toProcessor() {
      if (this instanceof MonoProcessor) {
         return (MonoProcessor<T>)this;
      } else {
         NextProcessor<T> result = new NextProcessor<>(this);
         result.connect();
         return result;
      }
   }

   public final <V> Mono<V> transform(Function<? super Mono<T>, ? extends Publisher<V>> transformer) {
      if (Hooks.DETECT_CONTEXT_LOSS) {
         transformer = new ContextTrackingFunctionWrapper<>(transformer);
      }

      return onAssembly(from((Publisher<? extends V>)transformer.apply(this)));
   }

   public final <V> Mono<V> transformDeferred(Function<? super Mono<T>, ? extends Publisher<V>> transformer) {
      return defer(
         () -> Hooks.DETECT_CONTEXT_LOSS
               ? from(new ContextTrackingFunctionWrapper(transformer).apply(this))
               : from((Publisher<? extends T>)transformer.apply(this))
      );
   }

   public final <V> Mono<V> transformDeferredContextual(BiFunction<? super Mono<T>, ? super ContextView, ? extends Publisher<V>> transformer) {
      return deferContextual(
         ctxView -> {
            if (Hooks.DETECT_CONTEXT_LOSS) {
               ContextTrackingFunctionWrapper<T, V> wrapper = new ContextTrackingFunctionWrapper<>(
                  publisher -> (Publisher)transformer.apply(wrap(publisher, false), ctxView), transformer.toString()
               );
               return wrap(wrapper.apply(this), true);
            } else {
               return from((Publisher<? extends T>)transformer.apply(this, ctxView));
            }
         }
      );
   }

   public final <T2> Mono<Tuple2<T, T2>> zipWhen(Function<T, Mono<? extends T2>> rightGenerator) {
      return this.zipWhen(rightGenerator, Tuples::of);
   }

   public final <T2, O> Mono<O> zipWhen(Function<T, Mono<? extends T2>> rightGenerator, BiFunction<T, T2, O> combinator) {
      Objects.requireNonNull(rightGenerator, "rightGenerator function is mandatory to get the right-hand side Mono");
      Objects.requireNonNull(combinator, "combinator function is mandatory to combine results from both Monos");
      return this.flatMap(t -> ((Mono)rightGenerator.apply(t)).map(t2 -> combinator.apply(t, t2)));
   }

   public final <T2> Mono<Tuple2<T, T2>> zipWith(Mono<? extends T2> other) {
      return this.zipWith(other, Flux.tuple2Function());
   }

   public final <T2, O> Mono<O> zipWith(Mono<? extends T2> other, BiFunction<? super T, ? super T2, ? extends O> combinator) {
      if (this instanceof MonoZip) {
         MonoZip<T, O> o = (MonoZip)this;
         Mono<O> result = o.zipAdditionalSource(other, combinator);
         if (result != null) {
            return result;
         }
      }

      return zip(this, other, combinator);
   }

   protected static <T> Mono<T> onAssembly(Mono<T> source) {
      Function<Publisher, Publisher> hook = Hooks.onEachOperatorHook;
      if (hook != null) {
         source = (Mono)hook.apply(source);
      }

      if (Hooks.GLOBAL_TRACE) {
         FluxOnAssembly.AssemblySnapshot stacktrace = new FluxOnAssembly.AssemblySnapshot(null, (Supplier<String>)Traces.callSiteSupplierFactory.get());
         source = (Mono)Hooks.<T, Mono<T>>addAssemblyInfo(source, stacktrace);
      }

      return source;
   }

   public String toString() {
      return this.getClass().getSimpleName();
   }

   static <T> Mono<Void> empty(Publisher<T> source) {
      return ignoreElements(source);
   }

   static <T> Mono<T> doOnSignal(
      Mono<T> source,
      @Nullable Consumer<? super Subscription> onSubscribe,
      @Nullable Consumer<? super T> onNext,
      @Nullable LongConsumer onRequest,
      @Nullable Runnable onCancel
   ) {
      return source instanceof Fuseable
         ? onAssembly(new MonoPeekFuseable<>(source, onSubscribe, onNext, onRequest, onCancel))
         : onAssembly(new MonoPeek<>(source, onSubscribe, onNext, onRequest, onCancel));
   }

   static <T> Mono<T> doOnTerminalSignal(
      Mono<T> source,
      @Nullable Consumer<? super T> onSuccess,
      @Nullable Consumer<? super Throwable> onError,
      @Nullable BiConsumer<? super T, Throwable> onAfterTerminate
   ) {
      return onAssembly(new MonoPeekTerminal<>(source, onSuccess, onError, onAfterTerminate));
   }

   static <T> Mono<T> wrap(Publisher<T> source, boolean enforceMonoContract) {
      if (source instanceof Mono) {
         return (Mono<T>)source;
      } else if (source instanceof FluxSourceMono || source instanceof FluxSourceMonoFuseable) {
         Mono<T> extracted = ((FluxFromMonoOperator)source).source;
         return extracted;
      } else if (enforceMonoContract) {
         if (source instanceof Flux && source instanceof Callable) {
            Callable<T> m = (Callable)source;
            return Flux.wrapToMono(m);
         } else {
            return (Mono<T>)(source instanceof Flux ? new MonoNext<>((Flux<? extends T>)source) : new MonoFromPublisher<>(source));
         }
      } else if (source instanceof Flux && source instanceof Fuseable) {
         return new MonoSourceFluxFuseable<>((Flux<? extends T>)source);
      } else if (source instanceof Flux) {
         return new MonoSourceFlux<>((Flux<? extends T>)source);
      } else {
         return (Mono<T>)(source instanceof Fuseable ? new MonoSourceFuseable<>(source) : new MonoSource<>(source));
      }
   }

   static <T> BiPredicate<? super T, ? super T> equalsBiPredicate() {
      return EQUALS_BIPREDICATE;
   }
}
