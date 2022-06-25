package reactor.core.publisher;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

public abstract class Operators {
   static final Fuseable.ConditionalSubscriber<?> EMPTY_SUBSCRIBER = new Fuseable.ConditionalSubscriber<Object>() {
      @Override
      public void onSubscribe(Subscription s) {
         Throwable e = new IllegalStateException("onSubscribe should not be used");
         Operators.log.error("Unexpected call to Operators.emptySubscriber()", e);
      }

      @Override
      public void onNext(Object o) {
         Throwable e = new IllegalStateException("onNext should not be used, got " + o);
         Operators.log.error("Unexpected call to Operators.emptySubscriber()", e);
      }

      @Override
      public boolean tryOnNext(Object o) {
         Throwable e = new IllegalStateException("tryOnNext should not be used, got " + o);
         Operators.log.error("Unexpected call to Operators.emptySubscriber()", e);
         return false;
      }

      @Override
      public void onError(Throwable t) {
         Throwable e = new IllegalStateException("onError should not be used", t);
         Operators.log.error("Unexpected call to Operators.emptySubscriber()", e);
      }

      @Override
      public void onComplete() {
         Throwable e = new IllegalStateException("onComplete should not be used");
         Operators.log.error("Unexpected call to Operators.emptySubscriber()", e);
      }

      @Override
      public Context currentContext() {
         return Context.empty();
      }
   };
   static final Logger log = Loggers.getLogger(Operators.class);

   public static long addCap(long a, long b) {
      long res = a + b;
      return res < 0L ? Long.MAX_VALUE : res;
   }

   public static <T> long addCap(AtomicLongFieldUpdater<T> updater, T instance, long toAdd) {
      long r;
      long u;
      do {
         r = updater.get(instance);
         if (r == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
         }

         u = addCap(r, toAdd);
      } while(!updater.compareAndSet(instance, r, u));

      return r;
   }

   @Nullable
   public static <T> Fuseable.QueueSubscription<T> as(Subscription s) {
      return s instanceof Fuseable.QueueSubscription ? (Fuseable.QueueSubscription)s : null;
   }

   public static Subscription cancelledSubscription() {
      return Operators.CancelledSubscription.INSTANCE;
   }

   public static void complete(Subscriber<?> s) {
      s.onSubscribe(Operators.EmptySubscription.INSTANCE);
      s.onComplete();
   }

   public static <T> CoreSubscriber<T> drainSubscriber() {
      return Operators.DrainSubscriber.INSTANCE;
   }

   public static <T> CoreSubscriber<T> emptySubscriber() {
      return EMPTY_SUBSCRIBER;
   }

   public static Subscription emptySubscription() {
      return Operators.EmptySubscription.INSTANCE;
   }

   public static boolean canAppearAfterOnSubscribe(Subscription subscription) {
      return subscription == Operators.EmptySubscription.FROM_SUBSCRIBE_INSTANCE;
   }

   public static void error(Subscriber<?> s, Throwable e) {
      s.onSubscribe(Operators.EmptySubscription.INSTANCE);
      s.onError(e);
   }

   public static void reportThrowInSubscribe(CoreSubscriber<?> subscriber, Throwable e) {
      try {
         subscriber.onSubscribe(Operators.EmptySubscription.FROM_SUBSCRIBE_INSTANCE);
      } catch (Throwable var3) {
         Exceptions.throwIfFatal(var3);
         e.addSuppressed(var3);
      }

      subscriber.onError(onOperatorError(e, subscriber.currentContext()));
   }

   public static <I, O> Function<? super Publisher<I>, ? extends Publisher<O>> lift(
      BiFunction<Scannable, ? super CoreSubscriber<? super O>, ? extends CoreSubscriber<? super I>> lifter
   ) {
      return Operators.LiftFunction.liftScannable(null, lifter);
   }

   public static <O> Function<? super Publisher<O>, ? extends Publisher<O>> lift(
      Predicate<Scannable> filter, BiFunction<Scannable, ? super CoreSubscriber<? super O>, ? extends CoreSubscriber<? super O>> lifter
   ) {
      return Operators.LiftFunction.liftScannable(filter, lifter);
   }

   public static <I, O> Function<? super Publisher<I>, ? extends Publisher<O>> liftPublisher(
      BiFunction<Publisher, ? super CoreSubscriber<? super O>, ? extends CoreSubscriber<? super I>> lifter
   ) {
      return Operators.LiftFunction.liftPublisher(null, lifter);
   }

   public static <O> Function<? super Publisher<O>, ? extends Publisher<O>> liftPublisher(
      Predicate<Publisher> filter, BiFunction<Publisher, ? super CoreSubscriber<? super O>, ? extends CoreSubscriber<? super O>> lifter
   ) {
      return Operators.LiftFunction.liftPublisher(filter, lifter);
   }

   public static long multiplyCap(long a, long b) {
      long u = a * b;
      return (a | b) >>> 31 != 0L && u / a != b ? Long.MAX_VALUE : u;
   }

   static final <R> Function<Context, Context> discardLocalAdapter(Class<R> type, Consumer<? super R> discardHook) {
      Objects.requireNonNull(type, "onDiscard must be based on a type");
      Objects.requireNonNull(discardHook, "onDiscard must be provided a discardHook Consumer");
      Consumer<Object> safeConsumer = obj -> {
         if (type.isInstance(obj)) {
            discardHook.accept(type.cast(obj));
         }

      };
      return ctx -> {
         Consumer<Object> consumer = ctx.getOrDefault("reactor.onDiscard.local", null);
         return consumer == null ? ctx.put("reactor.onDiscard.local", safeConsumer) : ctx.put("reactor.onDiscard.local", safeConsumer.andThen(consumer));
      };
   }

   public static final Context enableOnDiscard(@Nullable Context target, Consumer<?> discardConsumer) {
      Objects.requireNonNull(discardConsumer, "discardConsumer must be provided");
      return target == null ? Context.of("reactor.onDiscard.local", discardConsumer) : target.put("reactor.onDiscard.local", discardConsumer);
   }

   public static <T> void onDiscard(@Nullable T element, Context context) {
      Consumer<Object> hook = context.getOrDefault("reactor.onDiscard.local", null);
      if (element != null && hook != null) {
         try {
            hook.accept(element);
         } catch (Throwable var4) {
            log.warn("Error in discard hook", var4);
         }
      }

   }

   public static <T> void onDiscardQueueWithClear(@Nullable Queue<T> queue, Context context, @Nullable Function<T, Stream<?>> extract) {
      if (queue != null) {
         Consumer<Object> hook = context.getOrDefault("reactor.onDiscard.local", null);
         if (hook == null) {
            queue.clear();
         } else {
            try {
               while(true) {
                  T toDiscard = (T)queue.poll();
                  if (toDiscard == null) {
                     break;
                  }

                  if (extract != null) {
                     try {
                        ((Stream)extract.apply(toDiscard)).forEach(elementToDiscard -> {
                           try {
                              hook.accept(elementToDiscard);
                           } catch (Throwable var3x) {
                              log.warn("Error while discarding item extracted from a queue element, continuing with next item", var3x);
                           }

                        });
                     } catch (Throwable var7) {
                        log.warn("Error while extracting items to discard from queue element, continuing with next queue element", var7);
                     }
                  } else {
                     try {
                        hook.accept(toDiscard);
                     } catch (Throwable var6) {
                        log.warn("Error while discarding a queue element, continuing with next queue element", var6);
                     }
                  }
               }
            } catch (Throwable var8) {
               log.warn("Cannot further apply discard hook while discarding and clearing a queue", var8);
            }

         }
      }
   }

   public static void onDiscardMultiple(Stream<?> multiple, Context context) {
      Consumer<Object> hook = context.getOrDefault("reactor.onDiscard.local", null);
      if (hook != null) {
         try {
            multiple.filter(Objects::nonNull).forEach(v -> {
               try {
                  hook.accept(v);
               } catch (Throwable var3) {
                  log.warn("Error while discarding a stream element, continuing with next element", var3);
               }

            });
         } catch (Throwable var4) {
            log.warn("Error while discarding stream, stopping", var4);
         }
      }

   }

   public static void onDiscardMultiple(@Nullable Collection<?> multiple, Context context) {
      if (multiple != null) {
         Consumer<Object> hook = context.getOrDefault("reactor.onDiscard.local", null);
         if (hook != null) {
            try {
               if (multiple.isEmpty()) {
                  return;
               }

               for(Object o : multiple) {
                  if (o != null) {
                     try {
                        hook.accept(o);
                     } catch (Throwable var6) {
                        log.warn("Error while discarding element from a Collection, continuing with next element", var6);
                     }
                  }
               }
            } catch (Throwable var7) {
               log.warn("Error while discarding collection, stopping", var7);
            }
         }

      }
   }

   public static void onDiscardMultiple(@Nullable Iterator<?> multiple, boolean knownToBeFinite, Context context) {
      if (multiple != null) {
         if (knownToBeFinite) {
            Consumer<Object> hook = context.getOrDefault("reactor.onDiscard.local", null);
            if (hook != null) {
               try {
                  multiple.forEachRemaining(o -> {
                     if (o != null) {
                        try {
                           hook.accept(o);
                        } catch (Throwable var3x) {
                           log.warn("Error while discarding element from an Iterator, continuing with next element", var3x);
                        }
                     }

                  });
               } catch (Throwable var5) {
                  log.warn("Error while discarding Iterator, stopping", var5);
               }
            }

         }
      }
   }

   public static void onErrorDropped(Throwable e, Context context) {
      Consumer<? super Throwable> hook = context.getOrDefault("reactor.onErrorDropped.local", null);
      if (hook == null) {
         hook = Hooks.onErrorDroppedHook;
      }

      if (hook == null) {
         log.error("Operator called default onErrorDropped", e);
      } else {
         hook.accept(e);
      }
   }

   public static <T> void onNextDropped(T t, Context context) {
      Objects.requireNonNull(t, "onNext");
      Objects.requireNonNull(context, "context");
      Consumer<Object> hook = context.getOrDefault("reactor.onNextDropped.local", null);
      if (hook == null) {
         hook = Hooks.onNextDroppedHook;
      }

      if (hook != null) {
         hook.accept(t);
      } else if (log.isDebugEnabled()) {
         log.debug("onNextDropped: " + t);
      }

   }

   public static Throwable onOperatorError(Throwable error, Context context) {
      return onOperatorError(null, error, context);
   }

   public static Throwable onOperatorError(@Nullable Subscription subscription, Throwable error, Context context) {
      return onOperatorError(subscription, error, null, context);
   }

   public static Throwable onOperatorError(@Nullable Subscription subscription, Throwable error, @Nullable Object dataSignal, Context context) {
      Exceptions.throwIfFatal(error);
      if (subscription != null) {
         subscription.cancel();
      }

      Throwable t = Exceptions.unwrap(error);
      BiFunction<? super Throwable, Object, ? extends Throwable> hook = context.getOrDefault("reactor.onOperatorError.local", null);
      if (hook == null) {
         hook = Hooks.onOperatorErrorHook;
      }

      if (hook == null) {
         if (dataSignal != null && dataSignal != t && dataSignal instanceof Throwable) {
            t = Exceptions.addSuppressed(t, (Throwable)dataSignal);
         }

         return t;
      } else {
         return (Throwable)hook.apply(error, dataSignal);
      }
   }

   public static RuntimeException onRejectedExecution(Throwable original, Context context) {
      return onRejectedExecution(original, null, null, null, context);
   }

   static final OnNextFailureStrategy onNextErrorStrategy(Context context) {
      OnNextFailureStrategy strategy = null;
      BiFunction<? super Throwable, Object, ? extends Throwable> fn = context.getOrDefault("reactor.onNextError.localStrategy", null);
      if (fn instanceof OnNextFailureStrategy) {
         strategy = (OnNextFailureStrategy)fn;
      } else if (fn != null) {
         strategy = new OnNextFailureStrategy.LambdaOnNextErrorStrategy(fn);
      }

      if (strategy == null) {
         strategy = Hooks.onNextErrorHook;
      }

      if (strategy == null) {
         strategy = OnNextFailureStrategy.STOP;
      }

      return strategy;
   }

   public static final BiFunction<? super Throwable, Object, ? extends Throwable> onNextErrorFunction(Context context) {
      return onNextErrorStrategy(context);
   }

   @Nullable
   public static <T> Throwable onNextError(@Nullable T value, Throwable error, Context context, Subscription subscriptionForCancel) {
      error = unwrapOnNextError(error);
      OnNextFailureStrategy strategy = onNextErrorStrategy(context);
      if (strategy.test(error, value)) {
         Throwable t = strategy.process(error, value, context);
         if (t != null) {
            subscriptionForCancel.cancel();
         }

         return t;
      } else {
         return onOperatorError(subscriptionForCancel, error, value, context);
      }
   }

   @Nullable
   public static <T> Throwable onNextError(@Nullable T value, Throwable error, Context context) {
      error = unwrapOnNextError(error);
      OnNextFailureStrategy strategy = onNextErrorStrategy(context);
      return strategy.test(error, value) ? strategy.process(error, value, context) : onOperatorError(null, error, value, context);
   }

   public static <T> Throwable onNextInnerError(Throwable error, Context context, @Nullable Subscription subscriptionForCancel) {
      error = unwrapOnNextError(error);
      OnNextFailureStrategy strategy = onNextErrorStrategy(context);
      if (strategy.test(error, null)) {
         Throwable t = strategy.process(error, null, context);
         if (t != null && subscriptionForCancel != null) {
            subscriptionForCancel.cancel();
         }

         return t;
      } else {
         return error;
      }
   }

   @Nullable
   public static <T> RuntimeException onNextPollError(@Nullable T value, Throwable error, Context context) {
      error = unwrapOnNextError(error);
      OnNextFailureStrategy strategy = onNextErrorStrategy(context);
      if (strategy.test(error, value)) {
         Throwable t = strategy.process(error, value, context);
         return t != null ? Exceptions.propagate(t) : null;
      } else {
         Throwable t = onOperatorError(null, error, value, context);
         return Exceptions.propagate(t);
      }
   }

   public static <T> CorePublisher<T> onLastAssembly(CorePublisher<T> source) {
      Function<Publisher, Publisher> hook = Hooks.onLastOperatorHook;
      if (hook == null) {
         return source;
      } else {
         Publisher<T> publisher = (Publisher)Objects.requireNonNull(hook.apply(source), "LastOperator hook returned null");
         return (CorePublisher<T>)(publisher instanceof CorePublisher ? (CorePublisher)publisher : new Operators.CorePublisherAdapter<>(publisher));
      }
   }

   private static Throwable unwrapOnNextError(Throwable error) {
      return Exceptions.isBubbling(error) ? error : Exceptions.unwrap(error);
   }

   public static RuntimeException onRejectedExecution(
      Throwable original, @Nullable Subscription subscription, @Nullable Throwable suppressed, @Nullable Object dataSignal, Context context
   ) {
      if (context.hasKey("reactor.onRejectedExecution.local")) {
         context = context.put("reactor.onOperatorError.local", context.get("reactor.onRejectedExecution.local"));
      }

      RejectedExecutionException ree = Exceptions.failWithRejected(original);
      if (suppressed != null) {
         ree.addSuppressed(suppressed);
      }

      return dataSignal != null
         ? Exceptions.propagate(onOperatorError(subscription, ree, dataSignal, context))
         : Exceptions.propagate(onOperatorError(subscription, ree, context));
   }

   public static <T> long produced(AtomicLongFieldUpdater<T> updater, T instance, long toSub) {
      while(true) {
         long r = updater.get(instance);
         if (r != 0L && r != Long.MAX_VALUE) {
            long u = subOrZero(r, toSub);
            if (!updater.compareAndSet(instance, r, u)) {
               continue;
            }

            return u;
         }

         return r;
      }
   }

   public static <F> boolean replace(AtomicReferenceFieldUpdater<F, Subscription> field, F instance, Subscription s) {
      Subscription a;
      do {
         a = (Subscription)field.get(instance);
         if (a == Operators.CancelledSubscription.INSTANCE) {
            s.cancel();
            return false;
         }
      } while(!field.compareAndSet(instance, a, s));

      return true;
   }

   public static void reportBadRequest(long n) {
      if (log.isDebugEnabled()) {
         log.debug("Negative request", Exceptions.nullOrNegativeRequestException(n));
      }

   }

   public static void reportMoreProduced() {
      if (log.isDebugEnabled()) {
         log.debug("More data produced than requested", Exceptions.failWithOverflow());
      }

   }

   public static void reportSubscriptionSet() {
      if (log.isDebugEnabled()) {
         log.debug("Duplicate Subscription has been detected", Exceptions.duplicateOnSubscribeException());
      }

   }

   public static <T> Subscription scalarSubscription(CoreSubscriber<? super T> subscriber, T value) {
      return new Operators.ScalarSubscription<>(subscriber, value);
   }

   public static <T> Subscription scalarSubscription(CoreSubscriber<? super T> subscriber, T value, String stepName) {
      return new Operators.ScalarSubscription<>(subscriber, value, stepName);
   }

   public static <T> CoreSubscriber<T> serialize(CoreSubscriber<? super T> subscriber) {
      return new SerializedSubscriber<>(subscriber);
   }

   public static <F> boolean set(AtomicReferenceFieldUpdater<F, Subscription> field, F instance, Subscription s) {
      Subscription a;
      do {
         a = (Subscription)field.get(instance);
         if (a == Operators.CancelledSubscription.INSTANCE) {
            s.cancel();
            return false;
         }
      } while(!field.compareAndSet(instance, a, s));

      if (a != null) {
         a.cancel();
      }

      return true;
   }

   public static <F> boolean setOnce(AtomicReferenceFieldUpdater<F, Subscription> field, F instance, Subscription s) {
      Objects.requireNonNull(s, "subscription");
      Subscription a = (Subscription)field.get(instance);
      if (a == Operators.CancelledSubscription.INSTANCE) {
         s.cancel();
         return false;
      } else if (a != null) {
         s.cancel();
         reportSubscriptionSet();
         return false;
      } else if (field.compareAndSet(instance, null, s)) {
         return true;
      } else {
         a = (Subscription)field.get(instance);
         if (a == Operators.CancelledSubscription.INSTANCE) {
            s.cancel();
            return false;
         } else {
            s.cancel();
            reportSubscriptionSet();
            return false;
         }
      }
   }

   public static long subOrZero(long a, long b) {
      long res = a - b;
      return res < 0L ? 0L : res;
   }

   public static <F> boolean terminate(AtomicReferenceFieldUpdater<F, Subscription> field, F instance) {
      Subscription a = (Subscription)field.get(instance);
      if (a != Operators.CancelledSubscription.INSTANCE) {
         a = (Subscription)field.getAndSet(instance, Operators.CancelledSubscription.INSTANCE);
         if (a != null && a != Operators.CancelledSubscription.INSTANCE) {
            a.cancel();
            return true;
         }
      }

      return false;
   }

   public static boolean validate(@Nullable Subscription current, Subscription next) {
      Objects.requireNonNull(next, "Subscription cannot be null");
      if (current != null) {
         next.cancel();
         return false;
      } else {
         return true;
      }
   }

   public static boolean validate(long n) {
      if (n <= 0L) {
         reportBadRequest(n);
         return false;
      } else {
         return true;
      }
   }

   public static <T> CoreSubscriber<? super T> toCoreSubscriber(Subscriber<? super T> actual) {
      Objects.requireNonNull(actual, "actual");
      CoreSubscriber<? super T> _actual;
      if (actual instanceof CoreSubscriber) {
         _actual = (CoreSubscriber)actual;
      } else {
         _actual = new StrictSubscriber<>(actual);
      }

      return _actual;
   }

   public static <T> Fuseable.ConditionalSubscriber<? super T> toConditionalSubscriber(CoreSubscriber<? super T> actual) {
      Objects.requireNonNull(actual, "actual");
      Fuseable.ConditionalSubscriber<? super T> _actual;
      if (actual instanceof Fuseable.ConditionalSubscriber) {
         _actual = (Fuseable.ConditionalSubscriber)actual;
      } else {
         _actual = new Operators.ConditionalSubscriberAdapter<>(actual);
      }

      return _actual;
   }

   static Context multiSubscribersContext(InnerProducer<?>[] multicastInners) {
      return multicastInners.length > 0 ? multicastInners[0].actual().currentContext() : Context.empty();
   }

   static <T> long addCapCancellable(AtomicLongFieldUpdater<T> updater, T instance, long n) {
      while(true) {
         long r = updater.get(instance);
         if (r != Long.MIN_VALUE && r != Long.MAX_VALUE) {
            long u = addCap(r, n);
            if (!updater.compareAndSet(instance, r, u)) {
               continue;
            }

            return r;
         }

         return r;
      }
   }

   static void onErrorDroppedMulticast(Throwable e, InnerProducer<?>[] multicastInners) {
      onErrorDropped(e, multiSubscribersContext(multicastInners));
   }

   static <T> void onNextDroppedMulticast(T t, InnerProducer<?>[] multicastInners) {
      onNextDropped(t, multiSubscribersContext(multicastInners));
   }

   static <T> long producedCancellable(AtomicLongFieldUpdater<T> updater, T instance, long n) {
      long current;
      long update;
      do {
         current = updater.get(instance);
         if (current == Long.MIN_VALUE) {
            return Long.MIN_VALUE;
         }

         if (current == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
         }

         update = current - n;
         if (update < 0L) {
            reportBadRequest(update);
            update = 0L;
         }
      } while(!updater.compareAndSet(instance, current, update));

      return update;
   }

   static long unboundedOrPrefetch(int prefetch) {
      return prefetch == Integer.MAX_VALUE ? Long.MAX_VALUE : (long)prefetch;
   }

   static int unboundedOrLimit(int prefetch) {
      return prefetch == Integer.MAX_VALUE ? Integer.MAX_VALUE : prefetch - (prefetch >> 2);
   }

   static int unboundedOrLimit(int prefetch, int lowTide) {
      if (lowTide <= 0) {
         return prefetch;
      } else if (lowTide >= prefetch) {
         return unboundedOrLimit(prefetch);
      } else {
         return prefetch == Integer.MAX_VALUE ? Integer.MAX_VALUE : lowTide;
      }
   }

   Operators() {
   }

   static final class CancelledSubscription implements Subscription, Scannable {
      static final Operators.CancelledSubscription INSTANCE = new Operators.CancelledSubscription();

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.CANCELLED ? true : null;
      }

      @Override
      public void cancel() {
      }

      @Override
      public void request(long n) {
      }

      @Override
      public String stepName() {
         return "cancelledSubscription";
      }
   }

   static final class ConditionalSubscriberAdapter<T> implements Fuseable.ConditionalSubscriber<T> {
      final CoreSubscriber<T> delegate;

      ConditionalSubscriberAdapter(CoreSubscriber<T> delegate) {
         this.delegate = delegate;
      }

      @Override
      public Context currentContext() {
         return this.delegate.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         this.delegate.onSubscribe(s);
      }

      @Override
      public void onNext(T t) {
         this.delegate.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         this.delegate.onError(t);
      }

      @Override
      public void onComplete() {
         this.delegate.onComplete();
      }

      @Override
      public boolean tryOnNext(T t) {
         this.delegate.onNext(t);
         return true;
      }
   }

   static final class CorePublisherAdapter<T> implements CorePublisher<T>, OptimizableOperator<T, T> {
      final Publisher<T> publisher;
      @Nullable
      final OptimizableOperator<?, T> optimizableOperator;

      CorePublisherAdapter(Publisher<T> publisher) {
         this.publisher = publisher;
         if (publisher instanceof OptimizableOperator) {
            OptimizableOperator<?, T> optimSource = (OptimizableOperator)publisher;
            this.optimizableOperator = optimSource;
         } else {
            this.optimizableOperator = null;
         }

      }

      @Override
      public void subscribe(CoreSubscriber<? super T> subscriber) {
         this.publisher.subscribe(subscriber);
      }

      @Override
      public void subscribe(Subscriber<? super T> s) {
         this.publisher.subscribe(s);
      }

      @Override
      public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
         return actual;
      }

      @Override
      public final CorePublisher<? extends T> source() {
         return this;
      }

      @Override
      public final OptimizableOperator<?, ? extends T> nextOptimizableSource() {
         return this.optimizableOperator;
      }
   }

   public static class DeferredSubscription implements Subscription, Scannable {
      static final int STATE_CANCELLED = -2;
      static final int STATE_SUBSCRIBED = -1;
      Subscription s;
      volatile long requested;
      static final AtomicLongFieldUpdater<Operators.DeferredSubscription> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         Operators.DeferredSubscription.class, "requested"
      );

      protected boolean isCancelled() {
         return this.requested == -2L;
      }

      @Override
      public void cancel() {
         long state = REQUESTED.getAndSet(this, -2L);
         if (state != -2L) {
            if (state == -1L) {
               this.s.cancel();
            }

         }
      }

      protected void terminate() {
         REQUESTED.getAndSet(this, -2L);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         long requested = this.requested;
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return requested < 0L ? 0L : requested;
         } else {
            return key == Scannable.Attr.CANCELLED ? this.isCancelled() : null;
         }
      }

      @Override
      public void request(long n) {
         long r = this.requested;
         if (r > -1L) {
            do {
               if (r == Long.MAX_VALUE) {
                  return;
               }

               long u = Operators.addCap(r, n);
               if (REQUESTED.compareAndSet(this, r, u)) {
                  return;
               }

               r = this.requested;
            } while(r >= 0L);
         }

         if (r != -2L) {
            this.s.request(n);
         }
      }

      public final boolean set(Subscription s) {
         Objects.requireNonNull(s, "s");
         long state = this.requested;
         Subscription a = this.s;
         if (state == -2L) {
            s.cancel();
            return false;
         } else if (a != null) {
            s.cancel();
            Operators.reportSubscriptionSet();
            return false;
         } else {
            long accumulated = 0L;

            long r;
            do {
               r = this.requested;
               if (r == -2L || r == -1L) {
                  s.cancel();
                  return false;
               }

               this.s = s;
               long toRequest = r - accumulated;
               if (toRequest > 0L) {
                  s.request(toRequest);
               }

               accumulated += toRequest;
            } while(!REQUESTED.compareAndSet(this, r, -1L));

            return true;
         }
      }
   }

   static final class DrainSubscriber<T> implements CoreSubscriber<T> {
      static final Operators.DrainSubscriber INSTANCE = new Operators.DrainSubscriber();

      @Override
      public void onSubscribe(Subscription s) {
         s.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(Object o) {
      }

      @Override
      public void onError(Throwable t) {
         Operators.onErrorDropped(Exceptions.errorCallbackNotImplemented(t), Context.empty());
      }

      @Override
      public void onComplete() {
      }

      @Override
      public Context currentContext() {
         return Context.empty();
      }
   }

   static final class EmptySubscription implements Fuseable.QueueSubscription<Object>, Scannable {
      static final Operators.EmptySubscription INSTANCE = new Operators.EmptySubscription();
      static final Operators.EmptySubscription FROM_SUBSCRIBE_INSTANCE = new Operators.EmptySubscription();

      @Override
      public void cancel() {
      }

      public void clear() {
      }

      public boolean isEmpty() {
         return true;
      }

      @Nullable
      public Object poll() {
         return null;
      }

      @Override
      public void request(long n) {
      }

      @Override
      public int requestFusion(int requestedMode) {
         return 0;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.TERMINATED ? true : null;
      }

      public int size() {
         return 0;
      }

      @Override
      public String stepName() {
         return "emptySubscription";
      }
   }

   static final class LiftFunction<I, O> implements Function<Publisher<I>, Publisher<O>> {
      final Predicate<Publisher> filter;
      final String name;
      final BiFunction<Publisher, ? super CoreSubscriber<? super O>, ? extends CoreSubscriber<? super I>> lifter;

      static final <I, O> Operators.LiftFunction<I, O> liftScannable(
         @Nullable Predicate<Scannable> filter, BiFunction<Scannable, ? super CoreSubscriber<? super O>, ? extends CoreSubscriber<? super I>> lifter
      ) {
         Objects.requireNonNull(lifter, "lifter");
         Predicate<Publisher> effectiveFilter = null;
         if (filter != null) {
            effectiveFilter = pub -> filter.test(Scannable.from(pub));
         }

         BiFunction<Publisher, ? super CoreSubscriber<? super O>, ? extends CoreSubscriber<? super I>> effectiveLifter = (pub, sub) -> (CoreSubscriber)lifter.apply(
               Scannable.from(pub), sub
            );
         return new Operators.LiftFunction<>(effectiveFilter, effectiveLifter, lifter.toString());
      }

      static final <I, O> Operators.LiftFunction<I, O> liftPublisher(
         @Nullable Predicate<Publisher> filter, BiFunction<Publisher, ? super CoreSubscriber<? super O>, ? extends CoreSubscriber<? super I>> lifter
      ) {
         Objects.requireNonNull(lifter, "lifter");
         return new Operators.LiftFunction<>(filter, lifter, lifter.toString());
      }

      private LiftFunction(
         @Nullable Predicate<Publisher> filter,
         BiFunction<Publisher, ? super CoreSubscriber<? super O>, ? extends CoreSubscriber<? super I>> lifter,
         String name
      ) {
         this.filter = filter;
         this.lifter = (BiFunction)Objects.requireNonNull(lifter, "lifter");
         this.name = (String)Objects.requireNonNull(name, "name");
      }

      public Publisher<O> apply(Publisher<I> publisher) {
         if (this.filter != null && !this.filter.test(publisher)) {
            return publisher;
         } else if (publisher instanceof Fuseable) {
            if (publisher instanceof Mono) {
               return new MonoLiftFuseable<>(publisher, this);
            } else if (publisher instanceof ParallelFlux) {
               return new ParallelLiftFuseable<>((ParallelFlux<I>)publisher, this);
            } else if (publisher instanceof ConnectableFlux) {
               return new ConnectableLiftFuseable<>((ConnectableFlux<I>)publisher, this);
            } else {
               return (Publisher<O>)(publisher instanceof GroupedFlux
                  ? new GroupedLiftFuseable<>((GroupedFlux)publisher, this)
                  : new FluxLiftFuseable<>(publisher, this));
            }
         } else if (publisher instanceof Mono) {
            return new MonoLift<>(publisher, this);
         } else if (publisher instanceof ParallelFlux) {
            return new ParallelLift<>((ParallelFlux<I>)publisher, this);
         } else if (publisher instanceof ConnectableFlux) {
            return new ConnectableLift<>((ConnectableFlux<I>)publisher, this);
         } else {
            return (Publisher<O>)(publisher instanceof GroupedFlux ? new GroupedLift<>((GroupedFlux)publisher, this) : new FluxLift<>(publisher, this));
         }
      }
   }

   static class MonoInnerProducerBase<O> implements InnerProducer<O> {
      private final CoreSubscriber<? super O> actual;
      private O value;
      private volatile int state;
      private static final AtomicIntegerFieldUpdater<Operators.MonoInnerProducerBase> STATE = AtomicIntegerFieldUpdater.newUpdater(
         Operators.MonoInnerProducerBase.class, "state"
      );
      private static final int HAS_VALUE = 1;
      private static final int HAS_REQUEST = 2;
      private static final int HAS_COMPLETED = 4;
      private static final int CANCELLED = 128;

      public MonoInnerProducerBase(CoreSubscriber<? super O> actual) {
         this.actual = actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.isCancelled();
         } else if (key == Scannable.Attr.TERMINATED) {
            return hasCompleted(this.state);
         } else {
            return key == Scannable.Attr.PREFETCH ? Integer.MAX_VALUE : InnerProducer.super.scanUnsafe(key);
         }
      }

      public final void complete(O v) {
         int s;
         do {
            s = this.state;
            if (isCancelled(s)) {
               this.discard(v);
               return;
            }

            if (hasRequest(s) && STATE.compareAndSet(this, s, s | 5)) {
               this.value = null;
               this.doOnComplete(v);
               this.actual.onNext(v);
               this.actual.onComplete();
               return;
            }

            this.value = v;
         } while(!STATE.compareAndSet(this, s, s | 5));

      }

      public final void complete() {
         while(true) {
            int s = this.state;
            if (isCancelled(s)) {
               return;
            }

            if (STATE.compareAndSet(this, s, s | 4)) {
               if (hasValue(s) && hasRequest(s)) {
                  O v = this.value;
                  this.value = null;
                  this.doOnComplete(v);
                  this.actual.onNext(v);
                  this.actual.onComplete();
                  return;
               }

               if (!hasValue(s)) {
                  this.actual.onComplete();
                  return;
               }

               if (!hasRequest(s)) {
                  return;
               }
            }
         }
      }

      protected void doOnComplete(O v) {
      }

      protected final void discard(@Nullable O v) {
         Operators.onDiscard(v, this.actual.currentContext());
      }

      protected final void discardTheValue() {
         this.discard(this.value);
         this.value = null;
      }

      @Override
      public final CoreSubscriber<? super O> actual() {
         return this.actual;
      }

      public final boolean isCancelled() {
         return this.state == 128;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            int s;
            do {
               s = this.state;
               if (isCancelled(s)) {
                  return;
               }

               if (hasRequest(s)) {
                  return;
               }
            } while(!STATE.compareAndSet(this, s, s | 2));

            this.doOnRequest(n);
            if (hasValue(s) && hasCompleted(s)) {
               O v = this.value;
               this.value = null;
               this.doOnComplete(v);
               this.actual.onNext(v);
               this.actual.onComplete();
            }

         }
      }

      protected void doOnRequest(long n) {
      }

      protected final void setValue(@Nullable O value) {
         this.value = value;

         int s;
         do {
            s = this.state;
            if (isCancelled(s)) {
               this.discardTheValue();
               return;
            }
         } while(!STATE.compareAndSet(this, s, s | 1));

      }

      @Override
      public final void cancel() {
         int previous = STATE.getAndSet(this, 128);
         if (!isCancelled(previous)) {
            this.doOnCancel();
            if (hasValue(previous) && (previous & 6) != 6) {
               this.discardTheValue();
            }

         }
      }

      protected void doOnCancel() {
      }

      private static boolean isCancelled(int s) {
         return s == 128;
      }

      private static boolean hasRequest(int s) {
         return (s & 2) == 2;
      }

      private static boolean hasValue(int s) {
         return (s & 1) == 1;
      }

      private static boolean hasCompleted(int s) {
         return (s & 4) == 4;
      }
   }

   public static class MonoSubscriber<I, O> implements InnerOperator<I, O>, Fuseable, Fuseable.QueueSubscription<O> {
      protected final CoreSubscriber<? super O> actual;
      @Nullable
      protected O value;
      volatile int state;
      static final int NO_REQUEST_NO_VALUE = 0;
      static final int NO_REQUEST_HAS_VALUE = 1;
      static final int HAS_REQUEST_NO_VALUE = 2;
      static final int HAS_REQUEST_HAS_VALUE = 3;
      static final int CANCELLED = 4;
      static final int FUSED_EMPTY = 8;
      static final int FUSED_READY = 16;
      static final int FUSED_CONSUMED = 32;
      static final AtomicIntegerFieldUpdater<Operators.MonoSubscriber> STATE = AtomicIntegerFieldUpdater.newUpdater(Operators.MonoSubscriber.class, "state");

      public MonoSubscriber(CoreSubscriber<? super O> actual) {
         this.actual = actual;
      }

      @Override
      public void cancel() {
         O v = this.value;
         this.value = null;
         STATE.set(this, 4);
         this.discard(v);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.isCancelled();
         } else if (key != Scannable.Attr.TERMINATED) {
            return key == Scannable.Attr.PREFETCH ? Integer.MAX_VALUE : InnerOperator.super.scanUnsafe(key);
         } else {
            return this.state == 3 || this.state == 1;
         }
      }

      public final void clear() {
         STATE.lazySet(this, 32);
         this.value = null;
      }

      public final void complete(@Nullable O v) {
         int state;
         do {
            state = this.state;
            if (state == 8) {
               this.setValue(v);
               if (STATE.compareAndSet(this, 8, 16)) {
                  Subscriber<? super O> a = this.actual;
                  a.onNext(v);
                  a.onComplete();
                  return;
               }

               state = this.state;
            }

            if ((state & -3) != 0) {
               this.value = null;
               this.discard(v);
               return;
            }

            if (state == 2 && STATE.compareAndSet(this, 2, 3)) {
               this.value = null;
               Subscriber<? super O> a = this.actual;
               a.onNext(v);
               a.onComplete();
               return;
            }

            this.setValue(v);
         } while(state != 0 || !STATE.compareAndSet(this, 0, 1));

      }

      protected void discard(@Nullable O v) {
         Operators.onDiscard(v, this.actual.currentContext());
      }

      @Override
      public final CoreSubscriber<? super O> actual() {
         return this.actual;
      }

      public final boolean isCancelled() {
         return this.state == 4;
      }

      public final boolean isEmpty() {
         return this.state != 16;
      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
      }

      @Override
      public void onNext(I t) {
         this.setValue((O)t);
      }

      @Override
      public void onSubscribe(Subscription s) {
      }

      @Nullable
      public final O poll() {
         if (STATE.compareAndSet(this, 16, 32)) {
            O v = this.value;
            this.value = null;
            return v;
         } else {
            return null;
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            do {
               int s = this.state;
               if (s == 4) {
                  return;
               }

               if ((s & -2) != 0) {
                  return;
               }

               if (s == 1 && STATE.compareAndSet(this, 1, 3)) {
                  O v = this.value;
                  if (v != null) {
                     this.value = null;
                     Subscriber<? super O> a = this.actual;
                     a.onNext(v);
                     a.onComplete();
                  }

                  return;
               }
            } while(!STATE.compareAndSet(this, 0, 2));

         }
      }

      @Override
      public int requestFusion(int mode) {
         if ((mode & 2) != 0) {
            STATE.lazySet(this, 8);
            return 2;
         } else {
            return 0;
         }
      }

      public void setValue(@Nullable O value) {
         if (STATE.get(this) == 4) {
            this.discard(value);
         } else {
            this.value = value;
         }
      }

      public int size() {
         return this.isEmpty() ? 0 : 1;
      }
   }

   abstract static class MultiSubscriptionSubscriber<I, O> implements InnerOperator<I, O> {
      final CoreSubscriber<? super O> actual;
      protected boolean unbounded;
      Subscription subscription;
      long requested;
      volatile Subscription missedSubscription;
      volatile long missedRequested;
      volatile long missedProduced;
      volatile int wip;
      volatile boolean cancelled;
      static final AtomicReferenceFieldUpdater<Operators.MultiSubscriptionSubscriber, Subscription> MISSED_SUBSCRIPTION = AtomicReferenceFieldUpdater.newUpdater(
         Operators.MultiSubscriptionSubscriber.class, Subscription.class, "missedSubscription"
      );
      static final AtomicLongFieldUpdater<Operators.MultiSubscriptionSubscriber> MISSED_REQUESTED = AtomicLongFieldUpdater.newUpdater(
         Operators.MultiSubscriptionSubscriber.class, "missedRequested"
      );
      static final AtomicLongFieldUpdater<Operators.MultiSubscriptionSubscriber> MISSED_PRODUCED = AtomicLongFieldUpdater.newUpdater(
         Operators.MultiSubscriptionSubscriber.class, "missedProduced"
      );
      static final AtomicIntegerFieldUpdater<Operators.MultiSubscriptionSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         Operators.MultiSubscriptionSubscriber.class, "wip"
      );

      public MultiSubscriptionSubscriber(CoreSubscriber<? super O> actual) {
         this.actual = actual;
      }

      @Override
      public CoreSubscriber<? super O> actual() {
         return this.actual;
      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.drain();
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.missedSubscription != null ? this.missedSubscription : this.subscription;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.isCancelled();
         } else {
            return key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM
               ? Operators.addCap(this.requested, this.missedRequested)
               : InnerOperator.super.scanUnsafe(key);
         }
      }

      public final boolean isUnbounded() {
         return this.unbounded;
      }

      final boolean isCancelled() {
         return this.cancelled;
      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
      }

      @Override
      public void onSubscribe(Subscription s) {
         this.set(s);
      }

      public final void produced(long n) {
         if (!this.unbounded) {
            if (this.wip == 0 && WIP.compareAndSet(this, 0, 1)) {
               long r = this.requested;
               if (r != Long.MAX_VALUE) {
                  long u = r - n;
                  if (u < 0L) {
                     Operators.reportMoreProduced();
                     u = 0L;
                  }

                  this.requested = u;
               } else {
                  this.unbounded = true;
               }

               if (WIP.decrementAndGet(this) != 0) {
                  this.drainLoop();
               }
            } else {
               Operators.addCap(MISSED_PRODUCED, this, n);
               this.drain();
            }
         }
      }

      final void producedOne() {
         if (!this.unbounded) {
            if (this.wip == 0 && WIP.compareAndSet(this, 0, 1)) {
               long r = this.requested;
               if (r != Long.MAX_VALUE) {
                  --r;
                  if (r < 0L) {
                     Operators.reportMoreProduced();
                     r = 0L;
                  }

                  this.requested = r;
               } else {
                  this.unbounded = true;
               }

               if (WIP.decrementAndGet(this) != 0) {
                  this.drainLoop();
               }
            } else {
               Operators.addCap(MISSED_PRODUCED, this, 1L);
               this.drain();
            }
         }
      }

      @Override
      public final void request(long n) {
         if (Operators.validate(n)) {
            if (this.unbounded) {
               return;
            }

            if (this.wip == 0 && WIP.compareAndSet(this, 0, 1)) {
               long r = this.requested;
               if (r != Long.MAX_VALUE) {
                  r = Operators.addCap(r, n);
                  this.requested = r;
                  if (r == Long.MAX_VALUE) {
                     this.unbounded = true;
                  }
               }

               Subscription a = this.subscription;
               if (WIP.decrementAndGet(this) != 0) {
                  this.drainLoop();
               }

               if (a != null) {
                  a.request(n);
               }

               return;
            }

            Operators.addCap(MISSED_REQUESTED, this, n);
            this.drain();
         }

      }

      public final void set(Subscription s) {
         if (this.cancelled) {
            s.cancel();
         } else {
            Objects.requireNonNull(s);
            if (this.wip == 0 && WIP.compareAndSet(this, 0, 1)) {
               Subscription a = this.subscription;
               if (a != null && this.shouldCancelCurrent()) {
                  a.cancel();
               }

               this.subscription = s;
               long r = this.requested;
               if (WIP.decrementAndGet(this) != 0) {
                  this.drainLoop();
               }

               if (r != 0L) {
                  s.request(r);
               }

            } else {
               Subscription a = (Subscription)MISSED_SUBSCRIPTION.getAndSet(this, s);
               if (a != null && this.shouldCancelCurrent()) {
                  a.cancel();
               }

               this.drain();
            }
         }
      }

      protected boolean shouldCancelCurrent() {
         return false;
      }

      final void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            this.drainLoop();
         }
      }

      final void drainLoop() {
         int missed = 1;
         long requestAmount = 0L;
         long alreadyInRequestAmount = 0L;
         Subscription requestTarget = null;

         do {
            Subscription ms = this.missedSubscription;
            if (ms != null) {
               ms = (Subscription)MISSED_SUBSCRIPTION.getAndSet(this, null);
            }

            long mr = this.missedRequested;
            if (mr != 0L) {
               mr = MISSED_REQUESTED.getAndSet(this, 0L);
            }

            long mp = this.missedProduced;
            if (mp != 0L) {
               mp = MISSED_PRODUCED.getAndSet(this, 0L);
            }

            Subscription a = this.subscription;
            if (this.cancelled) {
               if (a != null) {
                  a.cancel();
                  this.subscription = null;
               }

               if (ms != null) {
                  ms.cancel();
               }
            } else {
               long r = this.requested;
               if (r != Long.MAX_VALUE) {
                  long u = Operators.addCap(r, mr);
                  if (u != Long.MAX_VALUE) {
                     long v = u - mp;
                     if (v < 0L) {
                        Operators.reportMoreProduced();
                        v = 0L;
                     }

                     r = v;
                  } else {
                     r = u;
                  }

                  this.requested = r;
               }

               if (ms != null) {
                  if (a != null && this.shouldCancelCurrent()) {
                     a.cancel();
                  }

                  this.subscription = ms;
                  if (r != 0L) {
                     requestAmount = Operators.addCap(requestAmount, r - alreadyInRequestAmount);
                     requestTarget = ms;
                  }
               } else if (mr != 0L && a != null) {
                  requestAmount = Operators.addCap(requestAmount, mr);
                  alreadyInRequestAmount += mr;
                  requestTarget = a;
               }
            }

            missed = WIP.addAndGet(this, -missed);
         } while(missed != 0);

         if (requestAmount != 0L) {
            requestTarget.request(requestAmount);
         }

      }
   }

   static final class ScalarSubscription<T> implements Fuseable.SynchronousSubscription<T>, InnerProducer<T> {
      final CoreSubscriber<? super T> actual;
      final T value;
      @Nullable
      final String stepName;
      volatile int once;
      static final AtomicIntegerFieldUpdater<Operators.ScalarSubscription> ONCE = AtomicIntegerFieldUpdater.newUpdater(
         Operators.ScalarSubscription.class, "once"
      );

      ScalarSubscription(CoreSubscriber<? super T> actual, T value) {
         this(actual, value, null);
      }

      ScalarSubscription(CoreSubscriber<? super T> actual, T value, String stepName) {
         this.value = (T)Objects.requireNonNull(value, "value");
         this.actual = (CoreSubscriber)Objects.requireNonNull(actual, "actual");
         this.stepName = stepName;
      }

      @Override
      public void cancel() {
         if (this.once == 0) {
            Operators.onDiscard(this.value, this.actual.currentContext());
         }

         ONCE.lazySet(this, 2);
      }

      public void clear() {
         if (this.once == 0) {
            Operators.onDiscard(this.value, this.actual.currentContext());
         }

         ONCE.lazySet(this, 1);
      }

      public boolean isEmpty() {
         return this.once != 0;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      public T poll() {
         if (this.once == 0) {
            ONCE.lazySet(this, 1);
            return this.value;
         } else {
            return null;
         }
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.once == 1;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.once == 2;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n) && ONCE.compareAndSet(this, 0, 1)) {
            Subscriber<? super T> a = this.actual;
            a.onNext(this.value);
            if (this.once != 2) {
               a.onComplete();
            }
         }

      }

      @Override
      public int requestFusion(int requestedMode) {
         return (requestedMode & 1) != 0 ? 1 : 0;
      }

      public int size() {
         return this.isEmpty() ? 0 : 1;
      }

      @Override
      public String stepName() {
         return this.stepName != null ? this.stepName : "scalarSubscription(" + this.value + ")";
      }
   }
}
