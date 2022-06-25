package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

final class FluxGenerate<T, S> extends Flux<T> implements Fuseable, SourceProducer<T> {
   static final Callable EMPTY_CALLABLE = () -> null;
   final Callable<S> stateSupplier;
   final BiFunction<S, SynchronousSink<T>, S> generator;
   final Consumer<? super S> stateConsumer;

   FluxGenerate(Consumer<SynchronousSink<T>> generator) {
      this(EMPTY_CALLABLE, (state, sink) -> {
         generator.accept(sink);
         return null;
      });
   }

   FluxGenerate(Callable<S> stateSupplier, BiFunction<S, SynchronousSink<T>, S> generator) {
      this(stateSupplier, generator, s -> {
      });
   }

   FluxGenerate(Callable<S> stateSupplier, BiFunction<S, SynchronousSink<T>, S> generator, Consumer<? super S> stateConsumer) {
      this.stateSupplier = (Callable)Objects.requireNonNull(stateSupplier, "stateSupplier");
      this.generator = (BiFunction)Objects.requireNonNull(generator, "generator");
      this.stateConsumer = (Consumer)Objects.requireNonNull(stateConsumer, "stateConsumer");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      S state;
      try {
         state = (S)this.stateSupplier.call();
      } catch (Throwable var4) {
         Operators.error(actual, Operators.onOperatorError(var4, actual.currentContext()));
         return;
      }

      actual.onSubscribe(new FluxGenerate.GenerateSubscription<>(actual, state, this.generator, this.stateConsumer));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static final class GenerateSubscription<T, S> implements Fuseable.QueueSubscription<T>, InnerProducer<T>, SynchronousSink<T> {
      final CoreSubscriber<? super T> actual;
      final BiFunction<S, SynchronousSink<T>, S> generator;
      final Consumer<? super S> stateConsumer;
      volatile boolean cancelled;
      S state;
      boolean terminate;
      boolean hasValue;
      boolean outputFused;
      T generatedValue;
      Throwable generatedError;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxGenerate.GenerateSubscription> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxGenerate.GenerateSubscription.class, "requested"
      );

      GenerateSubscription(CoreSubscriber<? super T> actual, S state, BiFunction<S, SynchronousSink<T>, S> generator, Consumer<? super S> stateConsumer) {
         this.actual = actual;
         this.state = state;
         this.generator = generator;
         this.stateConsumer = stateConsumer;
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
         if (key == Scannable.Attr.TERMINATED) {
            return this.terminate;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.ERROR) {
            return this.generatedError;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void next(T t) {
         if (this.terminate) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else if (this.hasValue) {
            this.error(new IllegalStateException("More than one call to onNext"));
         } else if (t == null) {
            this.error(new NullPointerException("The generator produced a null value"));
         } else {
            this.hasValue = true;
            if (this.outputFused) {
               this.generatedValue = t;
            } else {
               this.actual.onNext(t);
            }

         }
      }

      @Override
      public void error(Throwable e) {
         if (!this.terminate) {
            this.terminate = true;
            if (this.outputFused) {
               this.generatedError = e;
            } else {
               this.actual.onError(e);
            }

         }
      }

      @Override
      public void complete() {
         if (!this.terminate) {
            this.terminate = true;
            if (!this.outputFused) {
               this.actual.onComplete();
            }

         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n) && Operators.addCap(REQUESTED, this, n) == 0L) {
            if (n == Long.MAX_VALUE) {
               this.fastPath();
            } else {
               this.slowPath(n);
            }
         }

      }

      void fastPath() {
         S s = this.state;

         for(BiFunction<S, SynchronousSink<T>, S> g = this.generator; !this.cancelled; this.hasValue = false) {
            try {
               s = (S)g.apply(s, this);
            } catch (Throwable var4) {
               this.cleanup(s);
               this.actual.onError(Operators.onOperatorError(var4, this.actual.currentContext()));
               return;
            }

            if (this.terminate || this.cancelled) {
               this.cleanup(s);
               return;
            }

            if (!this.hasValue) {
               this.cleanup(s);
               this.actual.onError(new IllegalStateException("The generator didn't call any of the SynchronousSink method"));
               return;
            }
         }

         this.cleanup(s);
      }

      void slowPath(long n) {
         S s = this.state;
         long e = 0L;
         BiFunction<S, SynchronousSink<T>, S> g = this.generator;

         while(true) {
            while(e == n) {
               n = this.requested;
               if (n == e) {
                  this.state = s;
                  n = REQUESTED.addAndGet(this, -e);
                  e = 0L;
                  if (n == 0L) {
                     return;
                  }
               }
            }

            if (this.cancelled) {
               this.cleanup(s);
               return;
            }

            try {
               s = (S)g.apply(s, this);
            } catch (Throwable var8) {
               this.cleanup(s);
               this.actual.onError(var8);
               return;
            }

            if (this.terminate || this.cancelled) {
               this.cleanup(s);
               return;
            }

            if (!this.hasValue) {
               this.cleanup(s);
               this.actual.onError(new IllegalStateException("The generator didn't call any of the SynchronousSink method"));
               return;
            }

            ++e;
            this.hasValue = false;
         }
      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            if (REQUESTED.getAndIncrement(this) == 0L) {
               this.cleanup(this.state);
            }
         }

      }

      void cleanup(S s) {
         try {
            this.state = null;
            this.stateConsumer.accept(s);
         } catch (Throwable var3) {
            Operators.onErrorDropped(var3, this.actual.currentContext());
         }

      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 1) != 0 && (requestedMode & 4) == 0) {
            this.outputFused = true;
            return 1;
         } else {
            return 0;
         }
      }

      @Nullable
      public T poll() {
         S s = this.state;
         if (this.terminate) {
            this.cleanup(s);
            Throwable e = this.generatedError;
            if (e != null) {
               this.generatedError = null;
               throw Exceptions.propagate(e);
            } else {
               return null;
            }
         } else {
            try {
               s = (S)this.generator.apply(s, this);
            } catch (Throwable var3) {
               this.cleanup(s);
               throw var3;
            }

            if (!this.hasValue) {
               this.cleanup(s);
               if (!this.terminate) {
                  throw new IllegalStateException("The generator didn't call any of the SynchronousSink method");
               } else {
                  Throwable e = this.generatedError;
                  if (e != null) {
                     this.generatedError = null;
                     throw Exceptions.propagate(e);
                  } else {
                     return null;
                  }
               }
            } else {
               T v = this.generatedValue;
               this.generatedValue = null;
               this.hasValue = false;
               this.state = s;
               return v;
            }
         }
      }

      public boolean isEmpty() {
         return this.terminate;
      }

      public int size() {
         return this.isEmpty() ? 0 : -1;
      }

      public void clear() {
         this.generatedError = null;
         this.generatedValue = null;
      }
   }
}
