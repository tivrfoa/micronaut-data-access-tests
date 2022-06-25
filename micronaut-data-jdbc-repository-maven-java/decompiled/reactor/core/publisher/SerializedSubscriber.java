package reactor.core.publisher;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class SerializedSubscriber<T> implements InnerOperator<T, T> {
   final CoreSubscriber<? super T> actual;
   boolean drainLoopInProgress;
   boolean concurrentlyAddedContent;
   volatile boolean done;
   volatile boolean cancelled;
   SerializedSubscriber.LinkedArrayNode<T> head;
   SerializedSubscriber.LinkedArrayNode<T> tail;
   Throwable error;
   Subscription s;

   SerializedSubscriber(CoreSubscriber<? super T> actual) {
      this.actual = actual;
   }

   @Override
   public void onSubscribe(Subscription s) {
      if (Operators.validate(this.s, s)) {
         this.s = s;
         this.actual.onSubscribe(this);
      }

   }

   @Override
   public void onNext(T t) {
      if (this.cancelled) {
         Operators.onDiscard(t, this.actual.currentContext());
      } else if (this.done) {
         Operators.onNextDropped(t, this.actual.currentContext());
      } else {
         synchronized(this) {
            if (this.cancelled) {
               Operators.onDiscard(t, this.actual.currentContext());
               return;
            }

            if (this.done) {
               Operators.onNextDropped(t, this.actual.currentContext());
               return;
            }

            if (this.drainLoopInProgress) {
               this.serAdd(t);
               this.concurrentlyAddedContent = true;
               return;
            }

            this.drainLoopInProgress = true;
         }

         this.actual.onNext(t);
         this.serDrainLoop(this.actual);
      }
   }

   @Override
   public void onError(Throwable t) {
      if (!this.cancelled && !this.done) {
         synchronized(this) {
            if (this.cancelled || this.done) {
               return;
            }

            this.done = true;
            this.error = t;
            if (this.drainLoopInProgress) {
               this.concurrentlyAddedContent = true;
               return;
            }
         }

         this.actual.onError(t);
      }
   }

   @Override
   public void onComplete() {
      if (!this.cancelled && !this.done) {
         synchronized(this) {
            if (this.cancelled || this.done) {
               return;
            }

            this.done = true;
            if (this.drainLoopInProgress) {
               this.concurrentlyAddedContent = true;
               return;
            }
         }

         this.actual.onComplete();
      }
   }

   @Override
   public void request(long n) {
      this.s.request(n);
   }

   @Override
   public void cancel() {
      this.cancelled = true;
      this.s.cancel();
   }

   void serAdd(T value) {
      if (this.cancelled) {
         Operators.onDiscard(value, this.actual.currentContext());
      } else {
         SerializedSubscriber.LinkedArrayNode<T> t = this.tail;
         if (t == null) {
            t = new SerializedSubscriber.LinkedArrayNode<>(value);
            this.head = t;
            this.tail = t;
         } else if (t.count == 16) {
            SerializedSubscriber.LinkedArrayNode<T> n = new SerializedSubscriber.LinkedArrayNode<>(value);
            t.next = n;
            this.tail = n;
         } else {
            t.array[t.count++] = value;
         }

         if (this.cancelled) {
            Operators.onDiscard(value, this.actual.currentContext());
         }

      }
   }

   void serDrainLoop(CoreSubscriber<? super T> actual) {
      while(!this.cancelled) {
         boolean d;
         Throwable e;
         SerializedSubscriber.LinkedArrayNode<T> n;
         synchronized(this) {
            if (this.cancelled) {
               this.discardMultiple(this.head);
               return;
            }

            if (!this.concurrentlyAddedContent) {
               this.drainLoopInProgress = false;
               return;
            }

            this.concurrentlyAddedContent = false;
            d = this.done;
            e = this.error;
            n = this.head;
            this.head = null;
            this.tail = null;
         }

         while(n != null) {
            T[] arr = n.array;
            int c = n.count;

            for(int i = 0; i < c; ++i) {
               if (this.cancelled) {
                  synchronized(this) {
                     this.discardMultiple(n);
                     return;
                  }
               }

               actual.onNext(arr[i]);
            }

            n = n.next;
         }

         if (this.cancelled) {
            synchronized(this) {
               this.discardMultiple(this.head);
               return;
            }
         }

         if (e != null) {
            actual.onError(e);
            return;
         }

         if (d) {
            actual.onComplete();
            return;
         }
      }

      synchronized(this) {
         this.discardMultiple(this.head);
      }
   }

   private void discardMultiple(SerializedSubscriber.LinkedArrayNode<T> head) {
      SerializedSubscriber.LinkedArrayNode<T> originalHead = head;
      SerializedSubscriber.LinkedArrayNode<T> h = head;

      while(h != null) {
         for(int i = 0; i < h.count; ++i) {
            Operators.onDiscard(h.array[i], this.actual.currentContext());
         }

         h = h.next;
         if (h == null && this.head != originalHead) {
            originalHead = this.head;
            h = originalHead;
         }
      }

   }

   @Override
   public CoreSubscriber<? super T> actual() {
      return this.actual;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.s;
      } else if (key == Scannable.Attr.ERROR) {
         return this.error;
      } else if (key == Scannable.Attr.BUFFERED) {
         return this.producerCapacity();
      } else if (key == Scannable.Attr.CAPACITY) {
         return 16;
      } else if (key == Scannable.Attr.CANCELLED) {
         return this.cancelled;
      } else {
         return key == Scannable.Attr.TERMINATED ? this.done : InnerOperator.super.scanUnsafe(key);
      }
   }

   int producerCapacity() {
      SerializedSubscriber.LinkedArrayNode<T> node = this.tail;
      return node != null ? node.count : 0;
   }

   static final class LinkedArrayNode<T> {
      static final int DEFAULT_CAPACITY = 16;
      final T[] array = (T[])(new Object[16]);
      int count;
      SerializedSubscriber.LinkedArrayNode<T> next;

      LinkedArrayNode(T value) {
         this.array[0] = value;
         this.count = 1;
      }
   }
}
