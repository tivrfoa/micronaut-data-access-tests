package io.micronaut.http.server.netty.jackson;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.async.subscriber.CompletionAwareSubscriber;
import io.micronaut.core.async.subscriber.TypedSubscriber;
import io.micronaut.core.type.Argument;
import io.micronaut.http.MediaType;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.netty.AbstractHttpContentProcessor;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;
import java.util.Optional;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Internal
public class JsonContentProcessor extends AbstractHttpContentProcessor<JsonNode> {
   private final JsonMapper jsonMapper;
   private Processor<byte[], JsonNode> jacksonProcessor;

   public JsonContentProcessor(NettyHttpRequest<?> nettyHttpRequest, HttpServerConfiguration configuration, JsonMapper jsonMapper) {
      super(nettyHttpRequest, configuration);
      this.jsonMapper = jsonMapper;
   }

   @Override
   protected void doOnSubscribe(Subscription subscription, Subscriber<? super JsonNode> subscriber) {
      if (this.parentSubscription != null) {
         boolean streamArray = false;
         boolean isJsonStream = this.nettyHttpRequest.getContentType().map(mediaType -> mediaType.equals(MediaType.APPLICATION_JSON_STREAM_TYPE)).orElse(false);
         if (subscriber instanceof TypedSubscriber) {
            TypedSubscriber typedSubscriber = (TypedSubscriber)subscriber;
            Argument typeArgument = typedSubscriber.getTypeArgument();
            Class targetType = typeArgument.getType();
            if (Publishers.isConvertibleToPublisher(targetType) && !Publishers.isSingle(targetType)) {
               Optional<Argument<?>> genericArgument = typeArgument.getFirstTypeVariable();
               if (genericArgument.isPresent() && !Iterable.class.isAssignableFrom(((Argument)genericArgument.get()).getType()) && !isJsonStream) {
                  streamArray = true;
               }
            }
         }

         this.jacksonProcessor = this.jsonMapper.createReactiveParser(p -> {
         }, streamArray);
         this.jacksonProcessor.subscribe(new CompletionAwareSubscriber<JsonNode>() {
            @Override
            protected void doOnSubscribe(Subscription jsonSubscription) {
               Subscription childSubscription = new Subscription() {
                  boolean first = true;

                  @Override
                  public synchronized void request(long n) {
                     if (this.first) {
                        jsonSubscription.request(n < Long.MAX_VALUE ? n + 1L : n);
                        JsonContentProcessor.this.parentSubscription.request(n < Long.MAX_VALUE ? n + 1L : n);
                     } else {
                        jsonSubscription.request(n);
                        JsonContentProcessor.this.parentSubscription.request(n);
                     }

                  }

                  @Override
                  public synchronized void cancel() {
                     jsonSubscription.cancel();
                     JsonContentProcessor.this.parentSubscription.cancel();
                  }
               };
               subscriber.onSubscribe(childSubscription);
            }

            protected void doOnNext(JsonNode message) {
               subscriber.onNext(message);
            }

            @Override
            protected void doOnError(Throwable t) {
               subscriber.onError(t);
            }

            @Override
            protected void doOnComplete() {
               subscriber.onComplete();
            }
         });
         this.jacksonProcessor.onSubscribe(subscription);
      }
   }

   @Override
   protected void onData(ByteBufHolder message) {
      ByteBuf content = message.content();

      try {
         byte[] bytes = ByteBufUtil.getBytes(content);
         this.jacksonProcessor.onNext(bytes);
      } finally {
         ReferenceCountUtil.release(content);
      }

   }

   @Override
   protected void doAfterOnError(Throwable throwable) {
      this.jacksonProcessor.onError(throwable);
   }

   @Override
   protected void doOnComplete() {
      this.jacksonProcessor.onComplete();
      super.doOnComplete();
   }
}
