package reactor.core.publisher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
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
import reactor.core.Disposables;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

public abstract class ParallelFlux<T> implements CorePublisher<T> {
   public static <T> ParallelFlux<T> from(Publisher<? extends T> source) {
      return from(source, Schedulers.DEFAULT_POOL_SIZE, Queues.SMALL_BUFFER_SIZE, Queues.small());
   }

   public static <T> ParallelFlux<T> from(Publisher<? extends T> source, int parallelism) {
      return from(source, parallelism, Queues.SMALL_BUFFER_SIZE, Queues.small());
   }

   public static <T> ParallelFlux<T> from(Publisher<? extends T> source, int parallelism, int prefetch, Supplier<Queue<T>> queueSupplier) {
      Objects.requireNonNull(queueSupplier, "queueSupplier");
      Objects.requireNonNull(source, "source");
      return onAssembly(new ParallelSource<>(source, parallelism, prefetch, queueSupplier));
   }

   @SafeVarargs
   public static <T> ParallelFlux<T> from(Publisher<T>... publishers) {
      return onAssembly(new ParallelArraySource<>(publishers));
   }

   public final <U> U as(Function<? super ParallelFlux<T>, U> converter) {
      return (U)converter.apply(this);
   }

   public final ParallelFlux<T> checkpoint() {
      FluxOnAssembly.AssemblySnapshot stacktrace = new FluxOnAssembly.CheckpointHeavySnapshot(null, (Supplier<String>)Traces.callSiteSupplierFactory.get());
      return new ParallelFluxOnAssembly<>(this, stacktrace);
   }

   public final ParallelFlux<T> checkpoint(String description) {
      return new ParallelFluxOnAssembly<>(this, new FluxOnAssembly.CheckpointLightSnapshot(description));
   }

   public final ParallelFlux<T> checkpoint(String description, boolean forceStackTrace) {
      FluxOnAssembly.AssemblySnapshot stacktrace;
      if (!forceStackTrace) {
         stacktrace = new FluxOnAssembly.CheckpointLightSnapshot(description);
      } else {
         stacktrace = new FluxOnAssembly.CheckpointHeavySnapshot(description, (Supplier<String>)Traces.callSiteSupplierFactory.get());
      }

      return new ParallelFluxOnAssembly<>(this, stacktrace);
   }

   public final <C> ParallelFlux<C> collect(Supplier<? extends C> collectionSupplier, BiConsumer<? super C, ? super T> collector) {
      return onAssembly(new ParallelCollect<>(this, collectionSupplier, collector));
   }

   public final Mono<List<T>> collectSortedList(Comparator<? super T> comparator) {
      return this.collectSortedList(comparator, 16);
   }

   public final Mono<List<T>> collectSortedList(Comparator<? super T> comparator, int capacityHint) {
      int ch = capacityHint / this.parallelism() + 1;
      ParallelFlux<List<T>> railReduced = this.reduce(() -> new ArrayList(ch), (a, b) -> {
         a.add(b);
         return a;
      });
      ParallelFlux<List<T>> railSorted = railReduced.map(list -> {
         list.sort(comparator);
         return list;
      });
      return railSorted.reduce((a, b) -> sortedMerger(a, b, comparator));
   }

   public final <R> ParallelFlux<R> concatMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {
      return this.concatMap(mapper, 2, FluxConcatMap.ErrorMode.IMMEDIATE);
   }

   public final <R> ParallelFlux<R> concatMap(Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch) {
      return this.concatMap(mapper, prefetch, FluxConcatMap.ErrorMode.IMMEDIATE);
   }

   public final <R> ParallelFlux<R> concatMapDelayError(Function<? super T, ? extends Publisher<? extends R>> mapper) {
      return this.concatMap(mapper, 2, FluxConcatMap.ErrorMode.END);
   }

   public final ParallelFlux<T> doAfterTerminate(Runnable afterTerminate) {
      Objects.requireNonNull(afterTerminate, "afterTerminate");
      return doOnSignal(this, null, null, null, null, afterTerminate, null, null, null);
   }

   public final ParallelFlux<T> doOnCancel(Runnable onCancel) {
      Objects.requireNonNull(onCancel, "onCancel");
      return doOnSignal(this, null, null, null, null, null, null, null, onCancel);
   }

   public final ParallelFlux<T> doOnComplete(Runnable onComplete) {
      Objects.requireNonNull(onComplete, "onComplete");
      return doOnSignal(this, null, null, null, onComplete, null, null, null, null);
   }

   public final ParallelFlux<T> doOnEach(Consumer<? super Signal<T>> signalConsumer) {
      Objects.requireNonNull(signalConsumer, "signalConsumer");
      return onAssembly(
         new ParallelDoOnEach<>(
            this,
            (ctx, v) -> signalConsumer.accept(Signal.next(v, ctx)),
            (ctx, e) -> signalConsumer.accept(Signal.error(e, ctx)),
            ctx -> signalConsumer.accept(Signal.complete(ctx))
         )
      );
   }

   public final ParallelFlux<T> doOnError(Consumer<? super Throwable> onError) {
      Objects.requireNonNull(onError, "onError");
      return doOnSignal(this, null, null, onError, null, null, null, null, null);
   }

   public final ParallelFlux<T> doOnSubscribe(Consumer<? super Subscription> onSubscribe) {
      Objects.requireNonNull(onSubscribe, "onSubscribe");
      return doOnSignal(this, null, null, null, null, null, onSubscribe, null, null);
   }

   public final ParallelFlux<T> doOnNext(Consumer<? super T> onNext) {
      Objects.requireNonNull(onNext, "onNext");
      return doOnSignal(this, onNext, null, null, null, null, null, null, null);
   }

   public final ParallelFlux<T> doOnRequest(LongConsumer onRequest) {
      Objects.requireNonNull(onRequest, "onRequest");
      return doOnSignal(this, null, null, null, null, null, null, onRequest, null);
   }

   public final ParallelFlux<T> doOnTerminate(Runnable onTerminate) {
      Objects.requireNonNull(onTerminate, "onTerminate");
      return doOnSignal(this, null, null, e -> onTerminate.run(), onTerminate, null, null, null, null);
   }

   public final ParallelFlux<T> filter(Predicate<? super T> predicate) {
      Objects.requireNonNull(predicate, "predicate");
      return onAssembly(new ParallelFilter<>(this, predicate));
   }

   public final <R> ParallelFlux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {
      return this.flatMap(mapper, false, Integer.MAX_VALUE, Queues.SMALL_BUFFER_SIZE);
   }

   public final <R> ParallelFlux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper, boolean delayError) {
      return this.flatMap(mapper, delayError, Integer.MAX_VALUE, Queues.SMALL_BUFFER_SIZE);
   }

   public final <R> ParallelFlux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper, boolean delayError, int maxConcurrency) {
      return this.flatMap(mapper, delayError, maxConcurrency, Queues.SMALL_BUFFER_SIZE);
   }

   public final <R> ParallelFlux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper, boolean delayError, int maxConcurrency, int prefetch) {
      return onAssembly(new ParallelFlatMap<>(this, mapper, delayError, maxConcurrency, Queues.get(maxConcurrency), prefetch, Queues.get(prefetch)));
   }

   public final Flux<GroupedFlux<Integer, T>> groups() {
      return Flux.onAssembly(new ParallelGroup<>(this));
   }

   public final ParallelFlux<T> hide() {
      return new ParallelFluxHide<>(this);
   }

   public final ParallelFlux<T> log() {
      return this.log(null, Level.INFO);
   }

   public final ParallelFlux<T> log(@Nullable String category) {
      return this.log(category, Level.INFO);
   }

   public final ParallelFlux<T> log(@Nullable String category, Level level, SignalType... options) {
      return this.log(category, level, false, options);
   }

   public final ParallelFlux<T> log(@Nullable String category, Level level, boolean showOperatorLine, SignalType... options) {
      return onAssembly(new ParallelLog<>(this, new SignalLogger<>(this, category, level, showOperatorLine, options)));
   }

   public final <U> ParallelFlux<U> map(Function<? super T, ? extends U> mapper) {
      Objects.requireNonNull(mapper, "mapper");
      return onAssembly(new ParallelMap<>(this, mapper));
   }

   public final ParallelFlux<T> name(String name) {
      return ParallelFluxName.createOrAppend(this, name);
   }

   public final Flux<T> ordered(Comparator<? super T> comparator) {
      return this.ordered(comparator, Queues.SMALL_BUFFER_SIZE);
   }

   public final Flux<T> ordered(Comparator<? super T> comparator, int prefetch) {
      return new ParallelMergeOrdered<>(this, prefetch, comparator);
   }

   public abstract int parallelism();

   public final Mono<T> reduce(BiFunction<T, T, T> reducer) {
      Objects.requireNonNull(reducer, "reducer");
      return Mono.onAssembly(new ParallelMergeReduce<>(this, reducer));
   }

   public final <R> ParallelFlux<R> reduce(Supplier<R> initialSupplier, BiFunction<R, ? super T, R> reducer) {
      Objects.requireNonNull(initialSupplier, "initialSupplier");
      Objects.requireNonNull(reducer, "reducer");
      return onAssembly(new ParallelReduceSeed<>(this, initialSupplier, reducer));
   }

   public final ParallelFlux<T> runOn(Scheduler scheduler) {
      return this.runOn(scheduler, Queues.SMALL_BUFFER_SIZE);
   }

   public final ParallelFlux<T> runOn(Scheduler scheduler, int prefetch) {
      Objects.requireNonNull(scheduler, "scheduler");
      return onAssembly(new ParallelRunOn<>(this, scheduler, prefetch, Queues.get(prefetch)));
   }

   public final Flux<T> sequential() {
      return this.sequential(Queues.SMALL_BUFFER_SIZE);
   }

   public final Flux<T> sequential(int prefetch) {
      return Flux.onAssembly(new ParallelMergeSequential<>(this, prefetch, Queues.get(prefetch)));
   }

   public final Flux<T> sorted(Comparator<? super T> comparator) {
      return this.sorted(comparator, 16);
   }

   public final Flux<T> sorted(Comparator<? super T> comparator, int capacityHint) {
      int ch = capacityHint / this.parallelism() + 1;
      ParallelFlux<List<T>> railReduced = this.reduce(() -> new ArrayList(ch), (a, b) -> {
         a.add(b);
         return a;
      });
      ParallelFlux<List<T>> railSorted = railReduced.map(list -> {
         list.sort(comparator);
         return list;
      });
      return Flux.onAssembly(new ParallelMergeSort<>(railSorted, comparator));
   }

   public abstract void subscribe(CoreSubscriber<? super T>[] var1);

   public final Disposable subscribe() {
      return this.subscribe(null, null, null);
   }

   public final Disposable subscribe(Consumer<? super T> onNext) {
      return this.subscribe(onNext, null, null);
   }

   public final Disposable subscribe(@Nullable Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
      return this.subscribe(onNext, onError, null);
   }

   public final Disposable subscribe(@Nullable Consumer<? super T> onNext, @Nullable Consumer<? super Throwable> onError, @Nullable Runnable onComplete) {
      return this.subscribe(onNext, onError, onComplete, null, (Context)null);
   }

   @Override
   public final void subscribe(CoreSubscriber<? super T> s) {
      FluxHide.SuppressFuseableSubscriber<T> subscriber = new FluxHide.SuppressFuseableSubscriber<>(Operators.toCoreSubscriber(s));
      this.sequential().subscribe(Operators.toCoreSubscriber(subscriber));
   }

   public final Disposable subscribe(
      @Nullable Consumer<? super T> onNext,
      @Nullable Consumer<? super Throwable> onError,
      @Nullable Runnable onComplete,
      @Nullable Consumer<? super Subscription> onSubscribe
   ) {
      return this.subscribe(onNext, onError, onComplete, onSubscribe, null);
   }

   public final Disposable subscribe(
      @Nullable Consumer<? super T> onNext, @Nullable Consumer<? super Throwable> onError, @Nullable Runnable onComplete, @Nullable Context initialContext
   ) {
      return this.subscribe(onNext, onError, onComplete, null, initialContext);
   }

   final Disposable subscribe(
      @Nullable Consumer<? super T> onNext,
      @Nullable Consumer<? super Throwable> onError,
      @Nullable Runnable onComplete,
      @Nullable Consumer<? super Subscription> onSubscribe,
      @Nullable Context initialContext
   ) {
      CorePublisher<T> publisher = Operators.onLastAssembly(this);
      if (!(publisher instanceof ParallelFlux)) {
         LambdaSubscriber<? super T> subscriber = new LambdaSubscriber<>(onNext, onError, onComplete, onSubscribe, initialContext);
         publisher.subscribe(Operators.toCoreSubscriber(new FluxHide.SuppressFuseableSubscriber<>(subscriber)));
         return subscriber;
      } else {
         LambdaSubscriber<? super T>[] subscribers = new LambdaSubscriber[this.parallelism()];
         int i = 0;

         while(i < subscribers.length) {
            subscribers[i++] = new LambdaSubscriber<>(onNext, onError, onComplete, onSubscribe, initialContext);
         }

         ((ParallelFlux)publisher).subscribe(subscribers);
         return Disposables.composite(subscribers);
      }
   }

   @Override
   public final void subscribe(Subscriber<? super T> s) {
      FluxHide.SuppressFuseableSubscriber<T> subscriber = new FluxHide.SuppressFuseableSubscriber<>(Operators.toCoreSubscriber(s));
      Operators.onLastAssembly(this.sequential()).subscribe(Operators.toCoreSubscriber(subscriber));
   }

   public final ParallelFlux<T> tag(String key, String value) {
      return ParallelFluxName.createOrAppend(this, key, value);
   }

   public final Mono<Void> then() {
      return Mono.onAssembly(new ParallelThen(this));
   }

   public final <U> ParallelFlux<U> transform(Function<? super ParallelFlux<T>, ParallelFlux<U>> composer) {
      return onAssembly(this.as(composer));
   }

   public final <U> ParallelFlux<U> transformGroups(Function<? super GroupedFlux<Integer, T>, ? extends Publisher<? extends U>> composer) {
      return this.getPrefetch() > -1
         ? from(this.groups().flatMap(composer::apply), this.parallelism(), this.getPrefetch(), Queues.small())
         : from(this.groups().flatMap(composer::apply), this.parallelism());
   }

   public String toString() {
      return this.getClass().getSimpleName();
   }

   protected final boolean validate(Subscriber<?>[] subscribers) {
      int p = this.parallelism();
      if (subscribers.length == p) {
         return true;
      } else {
         IllegalArgumentException iae = new IllegalArgumentException("parallelism = " + p + ", subscribers = " + subscribers.length);

         for(Subscriber<?> s : subscribers) {
            Operators.error(s, iae);
         }

         return false;
      }
   }

   final <R> ParallelFlux<R> concatMap(Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch, FluxConcatMap.ErrorMode errorMode) {
      return onAssembly(new ParallelConcatMap<>(this, mapper, Queues.get(prefetch), prefetch, errorMode));
   }

   final <R> ParallelFlux<R> concatMapDelayError(Function<? super T, ? extends Publisher<? extends R>> mapper, boolean delayUntilEnd, int prefetch) {
      return this.concatMap(mapper, prefetch, delayUntilEnd ? FluxConcatMap.ErrorMode.END : FluxConcatMap.ErrorMode.BOUNDARY);
   }

   final <R> ParallelFlux<R> concatMapDelayError(Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch) {
      return this.concatMap(mapper, prefetch, FluxConcatMap.ErrorMode.END);
   }

   public int getPrefetch() {
      return -1;
   }

   protected static <T> ParallelFlux<T> onAssembly(ParallelFlux<T> source) {
      Function<Publisher, Publisher> hook = Hooks.onEachOperatorHook;
      if (hook != null) {
         source = (ParallelFlux)hook.apply(source);
      }

      if (Hooks.GLOBAL_TRACE) {
         FluxOnAssembly.AssemblySnapshot stacktrace = new FluxOnAssembly.AssemblySnapshot(null, (Supplier<String>)Traces.callSiteSupplierFactory.get());
         source = (ParallelFlux)Hooks.<T, ParallelFlux<T>>addAssemblyInfo(source, stacktrace);
      }

      return source;
   }

   static <T> ParallelFlux<T> doOnSignal(
      ParallelFlux<T> source,
      @Nullable Consumer<? super T> onNext,
      @Nullable Consumer<? super T> onAfterNext,
      @Nullable Consumer<? super Throwable> onError,
      @Nullable Runnable onComplete,
      @Nullable Runnable onAfterTerminate,
      @Nullable Consumer<? super Subscription> onSubscribe,
      @Nullable LongConsumer onRequest,
      @Nullable Runnable onCancel
   ) {
      return onAssembly(new ParallelPeek<>(source, onNext, onAfterNext, onError, onComplete, onAfterTerminate, onSubscribe, onRequest, onCancel));
   }

   static final <T> List<T> sortedMerger(List<T> a, List<T> b, Comparator<? super T> comparator) {
      int n = a.size() + b.size();
      if (n == 0) {
         return new ArrayList();
      } else {
         List<T> both = new ArrayList(n);
         Iterator<T> at = a.iterator();
         Iterator<T> bt = b.iterator();
         T s1 = (T)(at.hasNext() ? at.next() : null);
         T s2 = (T)(bt.hasNext() ? bt.next() : null);

         while(s1 != null && s2 != null) {
            if (comparator.compare(s1, s2) < 0) {
               both.add(s1);
               s1 = (T)(at.hasNext() ? at.next() : null);
            } else {
               both.add(s2);
               s2 = (T)(bt.hasNext() ? bt.next() : null);
            }
         }

         if (s1 != null) {
            both.add(s1);

            while(at.hasNext()) {
               both.add(at.next());
            }
         } else if (s2 != null) {
            both.add(s2);

            while(bt.hasNext()) {
               both.add(bt.next());
            }
         }

         return both;
      }
   }
}
