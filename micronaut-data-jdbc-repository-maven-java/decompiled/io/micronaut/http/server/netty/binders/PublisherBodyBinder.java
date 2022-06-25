package io.micronaut.http.server.netty.binders;

import io.micronaut.core.async.subscriber.TypedSubscriber;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.DefaultBodyAnnotationBinder;
import io.micronaut.http.bind.binders.NonBlockingBodyArgumentBinder;
import io.micronaut.http.netty.stream.StreamedHttpRequest;
import io.micronaut.http.server.netty.HttpContentProcessor;
import io.micronaut.http.server.netty.HttpContentProcessorResolver;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.http.server.netty.NettyHttpServer;
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.EmptyByteBuf;
import io.netty.util.ReferenceCounted;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PublisherBodyBinder extends DefaultBodyAnnotationBinder<Publisher> implements NonBlockingBodyArgumentBinder<Publisher> {
   private static final Logger LOG = LoggerFactory.getLogger(NettyHttpServer.class);
   private static final Argument<Publisher> TYPE = Argument.of(Publisher.class);
   private final HttpContentProcessorResolver httpContentProcessorResolver;

   public PublisherBodyBinder(ConversionService conversionService, HttpContentProcessorResolver httpContentProcessorResolver) {
      super(conversionService);
      this.httpContentProcessorResolver = httpContentProcessorResolver;
   }

   @Override
   public Argument<Publisher> argumentType() {
      return TYPE;
   }

   @Override
   public ArgumentBinder.BindingResult<Publisher> bind(ArgumentConversionContext<Publisher> context, HttpRequest<?> source) {
      if (source instanceof NettyHttpRequest) {
         NettyHttpRequest nettyHttpRequest = (NettyHttpRequest)source;
         io.netty.handler.codec.http.HttpRequest nativeRequest = nettyHttpRequest.getNativeRequest();
         if (nativeRequest instanceof StreamedHttpRequest) {
            Argument<?> targetType = (Argument)context.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
            HttpContentProcessor<?> processor = this.httpContentProcessorResolver.resolve(nettyHttpRequest, targetType);
            return () -> Optional.of(
                  (Publisher<>)subscriber -> processor.subscribe(
                        new TypedSubscriber<Object>(context.getArgument()) {
                           Subscription s;
      
                           @Override
                           protected void doOnSubscribe(Subscription subscription) {
                              this.s = subscription;
                              subscriber.onSubscribe(subscription);
                           }
      
                           @Override
                           protected void doOnNext(Object message) {
                              if (PublisherBodyBinder.LOG.isTraceEnabled()) {
                                 PublisherBodyBinder.LOG.trace("Server received streaming message for argument [{}]: {}", context.getArgument(), message);
                              }
      
                              if (message instanceof ByteBufHolder) {
                                 message = ((ByteBufHolder)message).content();
                                 if (message instanceof EmptyByteBuf) {
                                    return;
                                 }
                              }
      
                              ArgumentConversionContext conversionContext = context.with(targetType);
                              Optional converted = PublisherBodyBinder.this.conversionService.convert(message, conversionContext);
                              if (converted.isPresent()) {
                                 subscriber.onNext(converted.get());
                              } else {
                                 try {
                                    Optional lastError = conversionContext.getLastError();
                                    if (lastError.isPresent()) {
                                       if (PublisherBodyBinder.LOG.isDebugEnabled()) {
                                          PublisherBodyBinder.LOG
                                             .debug(
                                                "Cannot convert message for argument [" + context.getArgument() + "] and value: " + message, lastError.get()
                                             );
                                       }
      
                                       subscriber.onError(new ConversionErrorException(context.getArgument(), (ConversionError)lastError.get()));
                                    } else {
                                       if (PublisherBodyBinder.LOG.isDebugEnabled()) {
                                          PublisherBodyBinder.LOG
                                             .debug("Cannot convert message for argument [{}] and value: {}", context.getArgument(), message);
                                       }
      
                                       subscriber.onError(UnsatisfiedRouteException.create(context.getArgument()));
                                    }
                                 } finally {
                                    this.s.cancel();
                                 }
                              }
      
                              if (message instanceof ReferenceCounted) {
                                 ((ReferenceCounted)message).release();
                              }
      
                           }
      
                           @Override
                           protected void doOnError(Throwable t) {
                              if (PublisherBodyBinder.LOG.isTraceEnabled()) {
                                 PublisherBodyBinder.LOG.trace("Server received error for argument [" + context.getArgument() + "]: " + t.getMessage(), t);
                              }
      
                              try {
                                 subscriber.onError(t);
                              } finally {
                                 this.s.cancel();
                              }
      
                           }
      
                           @Override
                           protected void doOnComplete() {
                              if (PublisherBodyBinder.LOG.isTraceEnabled()) {
                                 PublisherBodyBinder.LOG.trace("Done receiving messages for argument: {}", context.getArgument());
                              }
      
                              subscriber.onComplete();
                           }
                        }
                     )
               );
         }
      }

      return ArgumentBinder.BindingResult.EMPTY;
   }
}
