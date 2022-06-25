package io.micronaut.http.client.netty;

import io.netty.util.concurrent.Future;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

final class NettyFuturePublisher<T> implements Publisher<T> {
   private final Future<T> future;
   private final boolean forwardCancel;

   NettyFuturePublisher(Future<T> future, boolean forwardCancel) {
      this.future = future;
      this.forwardCancel = forwardCancel;
   }

   @Override
   public void subscribe(Subscriber<? super T> s) {
      s.onSubscribe(new Subscription() {
         boolean requested = false;

         @Override
         public void request(long n) {
            if (!this.requested) {
               this.requested = true;
               NettyFuturePublisher.this.future.addListener(f -> {
                  if (f.isSuccess()) {
                     s.onNext(f.getNow());
                     s.onComplete();
                  } else {
                     s.onError(f.cause());
                  }

               });
            }

         }

         @Override
         public void cancel() {
            if (NettyFuturePublisher.this.forwardCancel) {
               NettyFuturePublisher.this.future.cancel(true);
            }

         }
      });
   }
}
