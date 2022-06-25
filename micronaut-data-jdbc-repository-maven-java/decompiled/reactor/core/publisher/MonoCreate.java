package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

final class MonoCreate<T> extends Mono<T> implements SourceProducer<T> {
   static final Disposable TERMINATED = OperatorDisposables.DISPOSED;
   static final Disposable CANCELLED = Disposables.disposed();
   final Consumer<MonoSink<T>> callback;

   MonoCreate(Consumer<MonoSink<T>> callback) {
      this.callback = callback;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      MonoCreate.DefaultMonoSink<T> emitter = new MonoCreate.DefaultMonoSink<>(actual);
      actual.onSubscribe(emitter);

      try {
         this.callback.accept(emitter);
      } catch (Throwable var4) {
         emitter.error(Operators.onOperatorError(var4, actual.currentContext()));
      }

   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : null;
   }

   static final class DefaultMonoSink<T> extends AtomicBoolean implements MonoSink<T>, InnerProducer<T> {
      final CoreSubscriber<? super T> actual;
      volatile Disposable disposable;
      static final AtomicReferenceFieldUpdater<MonoCreate.DefaultMonoSink, Disposable> DISPOSABLE = AtomicReferenceFieldUpdater.newUpdater(
         MonoCreate.DefaultMonoSink.class, Disposable.class, "disposable"
      );
      volatile int state;
      static final AtomicIntegerFieldUpdater<MonoCreate.DefaultMonoSink> STATE = AtomicIntegerFieldUpdater.newUpdater(MonoCreate.DefaultMonoSink.class, "state");
      volatile LongConsumer requestConsumer;
      static final AtomicReferenceFieldUpdater<MonoCreate.DefaultMonoSink, LongConsumer> REQUEST_CONSUMER = AtomicReferenceFieldUpdater.newUpdater(
         MonoCreate.DefaultMonoSink.class, LongConsumer.class, "requestConsumer"
      );
      T value;
      static final int NO_REQUEST_HAS_VALUE = 1;
      static final int HAS_REQUEST_NO_VALUE = 2;
      static final int HAS_REQUEST_HAS_VALUE = 3;

      DefaultMonoSink(CoreSubscriber<? super T> actual) {
         this.actual = actual;
      }

      @Deprecated
      @Override
      public Context currentContext() {
         return this.actual.currentContext();
      }

      @Override
      public ContextView contextView() {
         return this.actual.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.CANCELLED) {
               return this.disposable == MonoCreate.CANCELLED;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerProducer.super.scanUnsafe(key);
            }
         } else {
            return this.state == 3 || this.state == 1 || this.disposable == MonoCreate.TERMINATED;
         }
      }

      @Override
      public void success() {
         if (!this.isDisposed()) {
            if (STATE.getAndSet(this, 3) != 3) {
               try {
                  this.actual.onComplete();
               } finally {
                  this.disposeResource(false);
               }
            }

         }
      }

      @Override
      public void success(@Nullable T value) {
         if (value == null) {
            this.success();
         } else {
            Disposable d = this.disposable;
            if (d == MonoCreate.CANCELLED) {
               Operators.onDiscard(value, this.actual.currentContext());
            } else if (d == MonoCreate.TERMINATED) {
               Operators.onNextDropped(value, this.actual.currentContext());
            } else {
               int s;
               do {
                  s = this.state;
                  if (s == 3 || s == 1) {
                     Operators.onNextDropped(value, this.actual.currentContext());
                     return;
                  }

                  if (s == 2) {
                     if (STATE.compareAndSet(this, s, 3)) {
                        try {
                           this.actual.onNext(value);
                           this.actual.onComplete();
                        } catch (Throwable var8) {
                           this.actual.onError(var8);
                        } finally {
                           this.disposeResource(false);
                        }
                     } else {
                        Operators.onNextDropped(value, this.actual.currentContext());
                     }

                     return;
                  }

                  this.value = value;
               } while(!STATE.compareAndSet(this, s, 1));

            }
         }
      }

      @Override
      public void error(Throwable e) {
         if (this.isDisposed()) {
            Operators.onOperatorError(e, this.actual.currentContext());
         } else {
            if (STATE.getAndSet(this, 3) != 3) {
               try {
                  this.actual.onError(e);
               } finally {
                  this.disposeResource(false);
               }
            } else {
               Operators.onOperatorError(e, this.actual.currentContext());
            }

         }
      }

      @Override
      public MonoSink<T> onRequest(LongConsumer consumer) {
         Objects.requireNonNull(consumer, "onRequest");
         if (!REQUEST_CONSUMER.compareAndSet(this, null, consumer)) {
            throw new IllegalStateException("A consumer has already been assigned to consume requests");
         } else {
            int s = this.state;
            if (s == 2 || s == 3) {
               consumer.accept(Long.MAX_VALUE);
            }

            return this;
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public MonoSink<T> onCancel(Disposable d) {
         Objects.requireNonNull(d, "onCancel");
         FluxCreate.SinkDisposable sd = new FluxCreate.SinkDisposable(null, d);
         if (!DISPOSABLE.compareAndSet(this, null, sd)) {
            Disposable c = this.disposable;
            if (c == MonoCreate.CANCELLED) {
               d.dispose();
            } else if (c instanceof FluxCreate.SinkDisposable) {
               FluxCreate.SinkDisposable current = (FluxCreate.SinkDisposable)c;
               if (current.onCancel == null) {
                  current.onCancel = d;
               } else {
                  d.dispose();
               }
            }
         }

         return this;
      }

      @Override
      public MonoSink<T> onDispose(Disposable d) {
         Objects.requireNonNull(d, "onDispose");
         FluxCreate.SinkDisposable sd = new FluxCreate.SinkDisposable(d, null);
         if (!DISPOSABLE.compareAndSet(this, null, sd)) {
            Disposable c = this.disposable;
            if (this.isDisposed()) {
               d.dispose();
            } else if (c instanceof FluxCreate.SinkDisposable) {
               FluxCreate.SinkDisposable current = (FluxCreate.SinkDisposable)c;
               if (current.disposable == null) {
                  current.disposable = d;
               } else {
                  d.dispose();
               }
            }
         }

         return this;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            LongConsumer consumer = this.requestConsumer;
            if (consumer != null) {
               consumer.accept(n);
            }

            int s;
            do {
               s = this.state;
               if (s == 2 || s == 3) {
                  return;
               }

               if (s == 1) {
                  if (STATE.compareAndSet(this, s, 3)) {
                     try {
                        this.actual.onNext(this.value);
                        this.actual.onComplete();
                     } finally {
                        this.disposeResource(false);
                     }
                  }

                  return;
               }
            } while(!STATE.compareAndSet(this, s, 2));

         }
      }

      @Override
      public void cancel() {
         if (STATE.getAndSet(this, 3) != 3) {
            T old = this.value;
            this.value = null;
            Operators.onDiscard(old, this.actual.currentContext());
            this.disposeResource(true);
         }

      }

      void disposeResource(boolean isCancel) {
         Disposable target = isCancel ? MonoCreate.CANCELLED : MonoCreate.TERMINATED;
         Disposable d = this.disposable;
         if (d != MonoCreate.TERMINATED && d != MonoCreate.CANCELLED) {
            d = (Disposable)DISPOSABLE.getAndSet(this, target);
            if (d != null && d != MonoCreate.TERMINATED && d != MonoCreate.CANCELLED) {
               if (isCancel && d instanceof FluxCreate.SinkDisposable) {
                  ((FluxCreate.SinkDisposable)d).cancel();
               }

               d.dispose();
            }
         }

      }

      public String toString() {
         return "MonoSink";
      }

      boolean isDisposed() {
         Disposable d = this.disposable;
         return d == MonoCreate.CANCELLED || d == MonoCreate.TERMINATED;
      }
   }
}
