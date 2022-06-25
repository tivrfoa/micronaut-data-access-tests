package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiConsumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

final class FluxHandleFuseable<T, R> extends InternalFluxOperator<T, R> implements Fuseable {
   final BiConsumer<? super T, SynchronousSink<R>> handler;

   FluxHandleFuseable(Flux<? extends T> source, BiConsumer<? super T, SynchronousSink<R>> handler) {
      super(source);
      this.handler = (BiConsumer)Objects.requireNonNull(handler, "handler");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      if (actual instanceof Fuseable.ConditionalSubscriber) {
         Fuseable.ConditionalSubscriber<? super R> cs = (Fuseable.ConditionalSubscriber)actual;
         return new FluxHandleFuseable.HandleFuseableConditionalSubscriber<>(cs, this.handler);
      } else {
         return new FluxHandleFuseable.HandleFuseableSubscriber<>(actual, this.handler);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class HandleFuseableConditionalSubscriber<T, R>
      implements Fuseable.ConditionalSubscriber<T>,
      InnerOperator<T, R>,
      Fuseable.QueueSubscription<R>,
      SynchronousSink<R> {
      final Fuseable.ConditionalSubscriber<? super R> actual;
      final BiConsumer<? super T, SynchronousSink<R>> handler;
      boolean done;
      boolean stop;
      Throwable error;
      R data;
      Fuseable.QueueSubscription<T> s;
      int sourceMode;

      HandleFuseableConditionalSubscriber(Fuseable.ConditionalSubscriber<? super R> actual, BiConsumer<? super T, SynchronousSink<R>> handler) {
         this.actual = actual;
         this.handler = handler;
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

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = (Fuseable.QueueSubscription)s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.sourceMode == 2) {
            this.actual.onNext((R)null);
         } else {
            if (this.done) {
               Operators.onNextDropped(t, this.actual.currentContext());
               return;
            }

            try {
               this.handler.accept(t, this);
            } catch (Throwable var4) {
               Throwable e_ = Operators.onNextError(t, var4, this.actual.currentContext(), this.s);
               if (e_ != null) {
                  this.onError(e_);
               } else {
                  this.reset();
                  this.s.request(1L);
               }

               return;
            }

            R v = this.data;
            this.data = null;
            if (v != null) {
               this.actual.onNext(v);
            }

            if (this.stop) {
               if (this.error != null) {
                  Throwable e_ = Operators.onNextError(t, this.error, this.actual.currentContext(), this.s);
                  if (e_ != null) {
                     this.done = true;
                     this.actual.onError(e_);
                  } else {
                     this.reset();
                     this.s.request(1L);
                  }
               } else {
                  this.done = true;
                  this.s.cancel();
                  this.actual.onComplete();
               }
            } else if (v == null) {
               this.s.request(1L);
            }
         }

      }

      private void reset() {
         this.done = false;
         this.stop = false;
         this.error = null;
      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
            return true;
         } else {
            try {
               this.handler.accept(t, this);
            } catch (Throwable var5) {
               Throwable e_ = Operators.onNextError(t, this.error, this.actual.currentContext(), this.s);
               if (e_ != null) {
                  this.onError(e_);
                  return true;
               }

               this.reset();
               return false;
            }

            R v = this.data;
            this.data = null;
            boolean emit = false;
            if (v != null) {
               emit = this.actual.tryOnNext(v);
            }

            if (this.stop) {
               if (this.error != null) {
                  Throwable e_ = Operators.onNextError(t, this.error, this.actual.currentContext(), this.s);
                  if (e_ == null) {
                     this.reset();
                     return false;
                  }

                  this.done = true;
                  this.actual.onError(e_);
               } else {
                  this.done = true;
                  this.s.cancel();
                  this.actual.onComplete();
               }

               return true;
            } else {
               return emit;
            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.actual.onComplete();
         }
      }

      @Override
      public void complete() {
         if (this.stop) {
            throw new IllegalStateException("Cannot complete after a complete or error");
         } else {
            this.stop = true;
         }
      }

      @Override
      public void error(Throwable e) {
         if (this.stop) {
            throw new IllegalStateException("Cannot error after a complete or error");
         } else {
            this.error = (Throwable)Objects.requireNonNull(e, "error");
            this.stop = true;
         }
      }

      @Override
      public void next(R o) {
         if (this.data != null) {
            throw new IllegalStateException("Cannot emit more than one data");
         } else if (this.stop) {
            throw new IllegalStateException("Cannot emit after a complete or error");
         } else {
            this.data = (R)Objects.requireNonNull(o, "data");
         }
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
      }

      @Nullable
      public R poll() {
         if (this.sourceMode == 2) {
            if (this.done) {
               return null;
            } else {
               long dropped = 0L;

               while(true) {
                  T v;
                  while(true) {
                     v = (T)this.s.poll();
                     if (v != null) {
                        try {
                           this.handler.accept(v, this);
                           break;
                        } catch (Throwable var7) {
                           RuntimeException e_ = Operators.onNextPollError(v, var7, this.actual.currentContext());
                           if (e_ != null) {
                              throw e_;
                           }

                           this.reset();
                        }
                     } else {
                        if (dropped == 0L) {
                           return null;
                        }

                        this.request(dropped);
                        dropped = 0L;
                     }
                  }

                  R u = this.data;
                  this.data = null;
                  if (this.stop) {
                     if (this.error == null) {
                        this.done = true;
                        this.s.cancel();
                        this.actual.onComplete();
                        return u;
                     }

                     Throwable e_ = Operators.onNextError(v, this.error, this.actual.currentContext(), this.s);
                     if (e_ != null) {
                        this.done = true;
                        throw Exceptions.propagate(e_);
                     }

                     this.reset();
                  } else {
                     if (u != null) {
                        return u;
                     }

                     ++dropped;
                  }
               }
            }
         } else {
            while(true) {
               T v = (T)this.s.poll();
               if (v == null) {
                  return null;
               }

               try {
                  this.handler.accept(v, this);
               } catch (Throwable var8) {
                  RuntimeException e_ = Operators.onNextPollError(v, var8, this.actual.currentContext());
                  if (e_ != null) {
                     throw e_;
                  }

                  this.reset();
                  continue;
               }

               R u = this.data;
               this.data = null;
               if (this.stop) {
                  this.done = true;
                  if (this.error == null) {
                     return u;
                  }

                  RuntimeException e_ = Operators.onNextPollError(v, this.error, this.actual.currentContext());
                  if (e_ != null) {
                     throw e_;
                  }

                  this.reset();
               } else if (u != null) {
                  return u;
               }
            }
         }
      }

      public boolean isEmpty() {
         return this.s.isEmpty();
      }

      public void clear() {
         this.s.clear();
      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 4) != 0) {
            return 0;
         } else {
            int m = this.s.requestFusion(requestedMode);
            this.sourceMode = m;
            return m;
         }
      }

      public int size() {
         return this.s.size();
      }
   }

   static final class HandleFuseableSubscriber<T, R>
      implements InnerOperator<T, R>,
      Fuseable.ConditionalSubscriber<T>,
      Fuseable.QueueSubscription<R>,
      SynchronousSink<R> {
      final CoreSubscriber<? super R> actual;
      final BiConsumer<? super T, SynchronousSink<R>> handler;
      boolean done;
      boolean stop;
      Throwable error;
      R data;
      Fuseable.QueueSubscription<T> s;
      int sourceMode;

      HandleFuseableSubscriber(CoreSubscriber<? super R> actual, BiConsumer<? super T, SynchronousSink<R>> handler) {
         this.actual = actual;
         this.handler = handler;
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

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
            return true;
         } else {
            try {
               this.handler.accept(t, this);
            } catch (Throwable var4) {
               Throwable e_ = Operators.onNextError(t, var4, this.actual.currentContext(), this.s);
               if (e_ != null) {
                  this.onError(e_);
                  return true;
               }

               this.reset();
               return false;
            }

            R v = this.data;
            this.data = null;
            if (v != null) {
               this.actual.onNext(v);
            }

            if (this.stop) {
               if (this.error != null) {
                  Throwable e_ = Operators.onNextError(t, this.error, this.actual.currentContext(), this.s);
                  if (e_ == null) {
                     this.reset();
                     return false;
                  }

                  this.done = true;
                  this.actual.onError(e_);
               } else {
                  this.done = true;
                  this.s.cancel();
                  this.actual.onComplete();
               }

               return true;
            } else {
               return v != null;
            }
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = (Fuseable.QueueSubscription)s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.sourceMode == 2) {
            this.actual.onNext((R)null);
         } else {
            if (this.done) {
               Operators.onNextDropped(t, this.actual.currentContext());
               return;
            }

            try {
               this.handler.accept(t, this);
            } catch (Throwable var4) {
               Throwable e_ = Operators.onNextError(t, var4, this.actual.currentContext(), this.s);
               if (e_ != null) {
                  this.onError(e_);
               } else {
                  this.s.request(1L);
               }

               return;
            }

            R v = this.data;
            this.data = null;
            if (v != null) {
               this.actual.onNext(v);
            }

            if (this.stop) {
               if (this.error != null) {
                  Throwable e_ = Operators.onNextError(t, this.error, this.actual.currentContext(), this.s);
                  if (e_ != null) {
                     this.done = true;
                     this.actual.onError(e_);
                  } else {
                     this.reset();
                     this.s.request(1L);
                  }
               } else {
                  this.done = true;
                  this.s.cancel();
                  this.actual.onComplete();
               }
            } else if (v == null) {
               this.s.request(1L);
            }
         }

      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.actual.onComplete();
         }
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
      }

      @Nullable
      public R poll() {
         if (this.sourceMode == 2) {
            if (this.done) {
               return null;
            } else {
               long dropped = 0L;

               while(true) {
                  T v;
                  while(true) {
                     v = (T)this.s.poll();
                     if (v != null) {
                        try {
                           this.handler.accept(v, this);
                           break;
                        } catch (Throwable var7) {
                           RuntimeException e_ = Operators.onNextPollError(v, var7, this.actual.currentContext());
                           if (e_ != null) {
                              throw e_;
                           }

                           this.reset();
                        }
                     } else {
                        if (dropped == 0L) {
                           return null;
                        }

                        this.request(dropped);
                        dropped = 0L;
                     }
                  }

                  R u = this.data;
                  this.data = null;
                  if (this.stop) {
                     if (this.error != null) {
                        RuntimeException e_ = Operators.onNextPollError(v, this.error, this.actual.currentContext());
                        if (e_ != null) {
                           this.done = true;
                           throw e_;
                        }
                     } else {
                        this.done = true;
                        this.s.cancel();
                        this.actual.onComplete();
                     }

                     return u;
                  }

                  if (u != null) {
                     return u;
                  }

                  ++dropped;
               }
            }
         } else {
            while(true) {
               T v = (T)this.s.poll();
               if (v == null) {
                  return null;
               }

               try {
                  this.handler.accept(v, this);
               } catch (Throwable var8) {
                  RuntimeException e_ = Operators.onNextPollError(v, var8, this.actual.currentContext());
                  if (e_ != null) {
                     throw e_;
                  }

                  this.reset();
                  continue;
               }

               R u = this.data;
               this.data = null;
               if (this.stop) {
                  if (this.error == null) {
                     this.done = true;
                     return u;
                  }

                  RuntimeException e_ = Operators.onNextPollError(v, this.error, this.actual.currentContext());
                  if (e_ != null) {
                     this.done = true;
                     throw e_;
                  }

                  this.reset();
               } else if (u != null) {
                  return u;
               }
            }
         }
      }

      private void reset() {
         this.done = false;
         this.stop = false;
         this.error = null;
      }

      public boolean isEmpty() {
         return this.s.isEmpty();
      }

      public void clear() {
         this.s.clear();
      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 4) != 0) {
            return 0;
         } else {
            int m = this.s.requestFusion(requestedMode);
            this.sourceMode = m;
            return m;
         }
      }

      public int size() {
         return this.s.size();
      }

      @Override
      public void complete() {
         if (this.stop) {
            throw new IllegalStateException("Cannot complete after a complete or error");
         } else {
            this.stop = true;
         }
      }

      @Override
      public void error(Throwable e) {
         if (this.stop) {
            throw new IllegalStateException("Cannot error after a complete or error");
         } else {
            this.error = (Throwable)Objects.requireNonNull(e, "error");
            this.stop = true;
         }
      }

      @Override
      public void next(R o) {
         if (this.data != null) {
            throw new IllegalStateException("Cannot emit more than one data");
         } else if (this.stop) {
            throw new IllegalStateException("Cannot emit after a complete or error");
         } else {
            this.data = (R)Objects.requireNonNull(o, "data");
         }
      }
   }
}
