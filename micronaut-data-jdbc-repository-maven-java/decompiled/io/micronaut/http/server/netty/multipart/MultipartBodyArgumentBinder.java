package io.micronaut.http.server.netty.multipart;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanProvider;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.subscriber.TypedSubscriber;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.bind.binders.NonBlockingBodyArgumentBinder;
import io.micronaut.http.netty.stream.StreamedHttpRequest;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.multipart.MultipartBody;
import io.micronaut.http.server.netty.DefaultHttpContentProcessor;
import io.micronaut.http.server.netty.HttpContentProcessor;
import io.micronaut.http.server.netty.HttpContentSubscriberFactory;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.http.server.netty.NettyHttpServer;
import io.micronaut.web.router.qualifier.ConsumesMediaTypeQualifier;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.EmptyByteBuf;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.util.ReferenceCountUtil;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public class MultipartBodyArgumentBinder implements NonBlockingBodyArgumentBinder<MultipartBody> {
   private static final Logger LOG = LoggerFactory.getLogger(NettyHttpServer.class);
   private final BeanLocator beanLocator;
   private final BeanProvider<HttpServerConfiguration> httpServerConfiguration;

   public MultipartBodyArgumentBinder(BeanLocator beanLocator, BeanProvider<HttpServerConfiguration> httpServerConfiguration) {
      this.beanLocator = beanLocator;
      this.httpServerConfiguration = httpServerConfiguration;
   }

   @Override
   public Argument<MultipartBody> argumentType() {
      return Argument.of(MultipartBody.class);
   }

   public ArgumentBinder.BindingResult<MultipartBody> bind(ArgumentConversionContext<MultipartBody> context, HttpRequest<?> source) {
      if (source instanceof NettyHttpRequest) {
         NettyHttpRequest nettyHttpRequest = (NettyHttpRequest)source;
         io.netty.handler.codec.http.HttpRequest nativeRequest = nettyHttpRequest.getNativeRequest();
         if (nativeRequest instanceof StreamedHttpRequest) {
            HttpContentProcessor<?> processor = (HttpContentProcessor)this.beanLocator
               .findBean(HttpContentSubscriberFactory.class, new ConsumesMediaTypeQualifier(MediaType.MULTIPART_FORM_DATA_TYPE))
               .map(factory -> factory.build(nettyHttpRequest))
               .orElse(new DefaultHttpContentProcessor(nettyHttpRequest, this.httpServerConfiguration.get()));
            return () -> Optional.of((MultipartBody)subscriber -> processor.subscribe(new TypedSubscriber<Object>(context.getArgument()) {
                     Subscription s;
                     AtomicLong partsRequested = new AtomicLong(0L);

                     @Override
                     protected void doOnSubscribe(Subscription subscription) {
                        this.s = subscription;
                        subscriber.onSubscribe(new Subscription() {
                           @Override
                           public void request(long n) {
                              if (partsRequested.getAndUpdate(prev -> prev + n) == 0L) {
                                 s.request(n);
                              }

                           }

                           @Override
                           public void cancel() {
                              subscription.cancel();
                           }
                        });
                     }

                     @Override
                     protected void doOnNext(Object message) {
                        if (MultipartBodyArgumentBinder.LOG.isTraceEnabled()) {
                           MultipartBodyArgumentBinder.LOG.trace("Server received streaming message for argument [{}]: {}", context.getArgument(), message);
                        }

                        if (!(message instanceof ByteBufHolder) || !(((ByteBufHolder)message).content() instanceof EmptyByteBuf)) {
                           if (message instanceof HttpData) {
                              HttpData data = (HttpData)message;
                              if (data.isCompleted()) {
                                 this.partsRequested.decrementAndGet();
                                 if (data instanceof FileUpload) {
                                    subscriber.onNext(new NettyCompletedFileUpload((FileUpload)data, false));
                                 } else if (data instanceof Attribute) {
                                    subscriber.onNext(new NettyCompletedAttribute((Attribute)data, false));
                                 }
                              }
                           }

                           ReferenceCountUtil.release(message);
                           if (this.partsRequested.get() > 0L) {
                              this.s.request(1L);
                           }

                        }
                     }

                     @Override
                     protected void doOnError(Throwable t) {
                        if (MultipartBodyArgumentBinder.LOG.isTraceEnabled()) {
                           MultipartBodyArgumentBinder.LOG.trace("Server received error for argument [" + context.getArgument() + "]: " + t.getMessage(), t);
                        }

                        try {
                           subscriber.onError(t);
                        } finally {
                           this.s.cancel();
                        }

                     }

                     @Override
                     protected void doOnComplete() {
                        if (MultipartBodyArgumentBinder.LOG.isTraceEnabled()) {
                           MultipartBodyArgumentBinder.LOG.trace("Done receiving messages for argument: {}", context.getArgument());
                        }

                        subscriber.onComplete();
                     }
                  }));
         }
      }

      return ArgumentBinder.BindingResult.EMPTY;
   }
}
