package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.MediaType;
import io.micronaut.http.exceptions.ContentLengthExceededException;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostStandardRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Internal
public class FormDataHttpContentProcessor extends AbstractHttpContentProcessor<HttpData> {
   private final InterfaceHttpPostRequestDecoder decoder;
   private final boolean enabled;
   private final AtomicLong extraMessages = new AtomicLong(0L);
   private final long partMaxSize;
   private volatile boolean pleaseDestroy = false;
   private volatile boolean inFlight = false;
   private boolean destroyed = false;

   FormDataHttpContentProcessor(NettyHttpRequest<?> nettyHttpRequest, NettyHttpServerConfiguration configuration) {
      super(nettyHttpRequest, configuration);
      Charset characterEncoding = nettyHttpRequest.getCharacterEncoding();
      HttpServerConfiguration.MultipartConfiguration multipart = configuration.getMultipart();
      DefaultHttpDataFactory factory;
      if (multipart.isDisk()) {
         factory = new DefaultHttpDataFactory(true, characterEncoding);
      } else if (multipart.isMixed()) {
         factory = new DefaultHttpDataFactory(multipart.getThreshold(), characterEncoding);
      } else {
         factory = new DefaultHttpDataFactory(false, characterEncoding);
      }

      factory.setMaxLimit(multipart.getMaxFileSize());
      HttpRequest nativeRequest = nettyHttpRequest.getNativeRequest();
      if (HttpPostRequestDecoder.isMultipart(nativeRequest)) {
         this.decoder = new MicronautHttpPostMultipartRequestDecoder(factory, nativeRequest, characterEncoding);
      } else {
         this.decoder = new HttpPostStandardRequestDecoder(factory, nativeRequest, characterEncoding);
      }

      this.enabled = nettyHttpRequest.getContentType().map(type -> type.equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE)).orElse(false)
         || multipart.isEnabled();
      this.partMaxSize = multipart.getMaxFileSize();
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   protected void doOnSubscribe(Subscription subscription, Subscriber<? super HttpData> subscriber) {
      subscriber.onSubscribe(new Subscription() {
         @Override
         public void request(long n) {
            FormDataHttpContentProcessor.this.extraMessages.updateAndGet(p -> {
               long newVal = p - n;
               if (newVal < 0L) {
                  subscription.request(n - p);
                  return 0L;
               } else {
                  return newVal;
               }
            });
         }

         @Override
         public void cancel() {
            subscription.cancel();
            FormDataHttpContentProcessor.this.pleaseDestroy = true;
            FormDataHttpContentProcessor.this.destroyIfRequested();
         }
      });
   }

   @Override
   protected void onData(ByteBufHolder message) {
      boolean skip;
      synchronized(this) {
         if (this.destroyed) {
            skip = true;
         } else {
            skip = false;
            this.inFlight = true;
         }
      }

      if (skip) {
         message.release();
      } else {
         Subscriber<? super HttpData> subscriber = this.getSubscriber();
         if (message instanceof HttpContent) {
            HttpContent httpContent = (HttpContent)message;
            List<InterfaceHttpData> messages = new ArrayList(1);

            try {
               InterfaceHttpPostRequestDecoder postRequestDecoder = this.decoder;
               postRequestDecoder.offer(httpContent);

               while(postRequestDecoder.hasNext()) {
                  InterfaceHttpData data = postRequestDecoder.next();
                  data.touch();
                  switch(data.getHttpDataType()) {
                     case Attribute:
                        Attribute attribute = (Attribute)data;
                        messages.add(attribute.retain());
                        postRequestDecoder.removeHttpDataFromClean(attribute);
                        break;
                     case FileUpload:
                        FileUpload fileUpload = (FileUpload)data;
                        if (fileUpload.isCompleted()) {
                           messages.add(fileUpload.retain());
                           postRequestDecoder.removeHttpDataFromClean(fileUpload);
                        }
                  }
               }

               InterfaceHttpData currentPartialHttpData = postRequestDecoder.currentPartialHttpData();
               if (currentPartialHttpData instanceof HttpData) {
                  messages.add(currentPartialHttpData.retain());
               }
            } catch (HttpPostRequestDecoder.EndOfDataDecoderException var26) {
            } catch (HttpPostRequestDecoder.ErrorDataDecoderException var27) {
               Throwable cause = var27.getCause();
               if (cause instanceof IOException && cause.getMessage().equals("Size exceed allowed maximum capacity")) {
                  String partName = this.decoder.currentPartialHttpData().getName();

                  try {
                     this.onError(
                        new ContentLengthExceededException(
                           "The part named [" + partName + "] exceeds the maximum allowed content length [" + this.partMaxSize + "]"
                        )
                     );
                  } finally {
                     this.parentSubscription.cancel();
                  }
               } else {
                  this.onError(var27);
               }
            } catch (Throwable var28) {
               this.onError(var28);
            } finally {
               if (messages.isEmpty()) {
                  this.subscription.request(1L);
               } else {
                  this.extraMessages.updateAndGet(p -> p + (long)messages.size() - 1L);
                  messages.stream().map(HttpData.class::cast).forEach(subscriber::onNext);
               }

               httpContent.release();
            }
         } else {
            message.release();
         }

         this.inFlight = false;
         this.destroyIfRequested();
      }
   }

   @Override
   protected void doAfterOnError(Throwable throwable) {
      this.pleaseDestroy = true;
      this.destroyIfRequested();
   }

   @Override
   protected void doAfterComplete() {
      this.pleaseDestroy = true;
      this.destroyIfRequested();
   }

   private void destroyIfRequested() {
      boolean destroy;
      synchronized(this) {
         if (this.pleaseDestroy && !this.destroyed && !this.inFlight) {
            destroy = true;
            this.destroyed = true;
         } else {
            destroy = false;
         }
      }

      if (destroy) {
         this.decoder.destroy();
      }

   }
}
