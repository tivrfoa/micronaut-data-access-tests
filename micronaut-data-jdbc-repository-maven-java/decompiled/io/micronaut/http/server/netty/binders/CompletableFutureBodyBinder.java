package io.micronaut.http.server.netty.binders;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.subscriber.CompletionAwareSubscriber;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.DefaultBodyAnnotationBinder;
import io.micronaut.http.bind.binders.NonBlockingBodyArgumentBinder;
import io.micronaut.http.netty.stream.StreamedHttpRequest;
import io.micronaut.http.server.netty.HttpContentProcessor;
import io.micronaut.http.server.netty.HttpContentProcessorResolver;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.netty.buffer.ByteBufHolder;
import io.netty.util.ReferenceCountUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import org.reactivestreams.Subscription;

@Internal
public class CompletableFutureBodyBinder extends DefaultBodyAnnotationBinder<CompletableFuture> implements NonBlockingBodyArgumentBinder<CompletableFuture> {
   private static final Argument<CompletableFuture> TYPE = Argument.of(CompletableFuture.class);
   private final HttpContentProcessorResolver httpContentProcessorResolver;

   public CompletableFutureBodyBinder(HttpContentProcessorResolver httpContentProcessorResolver, ConversionService conversionService) {
      super(conversionService);
      this.httpContentProcessorResolver = httpContentProcessorResolver;
   }

   @NonNull
   @Override
   public List<Class<?>> superTypes() {
      return Arrays.asList(CompletionStage.class, Future.class);
   }

   @Override
   public Argument<CompletableFuture> argumentType() {
      return TYPE;
   }

   @Override
   public ArgumentBinder.BindingResult<CompletableFuture> bind(ArgumentConversionContext<CompletableFuture> context, HttpRequest<?> source) {
      if (source instanceof NettyHttpRequest) {
         final NettyHttpRequest nettyHttpRequest = (NettyHttpRequest)source;
         io.netty.handler.codec.http.HttpRequest nativeRequest = ((NettyHttpRequest)source).getNativeRequest();
         if (nativeRequest instanceof StreamedHttpRequest) {
            final CompletableFuture future = new CompletableFuture();
            Argument<?> targetType = (Argument)context.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
            HttpContentProcessor<?> processor = this.httpContentProcessorResolver.resolve(nettyHttpRequest, targetType);
            processor.subscribe(new CompletionAwareSubscriber<Object>() {
               @Override
               protected void doOnSubscribe(Subscription subscription) {
                  subscription.request(1L);
               }

               @Override
               protected void doOnNext(Object message) {
                  if (message instanceof ByteBufHolder) {
                     nettyHttpRequest.addContent((ByteBufHolder)message);
                  } else {
                     nettyHttpRequest.setBody(message);
                  }

                  ReferenceCountUtil.release(message);
                  this.subscription.request(1L);
               }

               @Override
               protected void doOnError(Throwable t) {
                  future.completeExceptionally(t);
               }

               @Override
               protected void doOnComplete() {
                  Optional<Argument<?>> firstTypeParameter = context.getFirstTypeVariable();
                  if (firstTypeParameter.isPresent()) {
                     Argument<?> arg = (Argument)firstTypeParameter.get();
                     Optional converted = nettyHttpRequest.getBody(arg);
                     if (converted.isPresent()) {
                        future.complete(converted.get());
                     } else {
                        future.completeExceptionally(new IllegalArgumentException("Cannot bind body to argument type: " + arg.getType().getName()));
                     }
                  } else {
                     future.complete(nettyHttpRequest.getBody().orElse(null));
                  }

               }
            });
            return () -> Optional.of(future);
         } else {
            return ArgumentBinder.BindingResult.EMPTY;
         }
      } else {
         return ArgumentBinder.BindingResult.EMPTY;
      }
   }
}
