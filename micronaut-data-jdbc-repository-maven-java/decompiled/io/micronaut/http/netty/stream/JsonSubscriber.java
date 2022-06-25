package io.micronaut.http.netty.stream;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.netty.content.HttpContentUtil;
import io.netty.handler.codec.http.HttpContent;
import java.util.concurrent.atomic.AtomicBoolean;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

@Internal
public final class JsonSubscriber implements CoreSubscriber<HttpContent> {
   private final AtomicBoolean empty = new AtomicBoolean(true);
   private final CoreSubscriber<? super HttpContent> upstream;

   public JsonSubscriber(CoreSubscriber<? super HttpContent> upstream) {
      this.upstream = upstream;
   }

   @Override
   public Context currentContext() {
      return this.upstream.currentContext();
   }

   @Override
   public void onSubscribe(Subscription s) {
      this.upstream.onSubscribe(s);
   }

   public void onNext(HttpContent o) {
      if (this.empty.compareAndSet(true, false)) {
         this.upstream.onNext(HttpContentUtil.prefixOpenBracket(o));
      } else {
         this.upstream.onNext(HttpContentUtil.prefixComma(o));
      }

   }

   @Override
   public void onError(Throwable t) {
      this.upstream.onError(t);
   }

   @Override
   public void onComplete() {
      if (this.empty.get()) {
         this.upstream.onNext(HttpContentUtil.prefixOpenBracket(HttpContentUtil.closeBracket()));
      } else {
         this.upstream.onNext(HttpContentUtil.closeBracket());
      }

      this.upstream.onComplete();
   }

   public static Flux<HttpContent> lift(Publisher<HttpContent> publisher) {
      return (Flux<HttpContent>)Operators.lift((scannable, subscriber) -> new JsonSubscriber(subscriber)).apply(publisher);
   }
}
