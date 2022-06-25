package io.micronaut.http.server.netty.binders;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.subscriber.CompletionAwareSubscriber;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.NonBlockingBodyArgumentBinder;
import io.micronaut.http.netty.stream.StreamedHttpRequest;
import io.micronaut.http.server.netty.HttpContentProcessor;
import io.micronaut.http.server.netty.HttpContentProcessorResolver;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.http.server.netty.NettyHttpServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.EmptyByteBuf;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Internal
public class InputStreamBodyBinder implements NonBlockingBodyArgumentBinder<InputStream> {
   public static final Argument<InputStream> TYPE = Argument.of(InputStream.class);
   private static final Logger LOG = LoggerFactory.getLogger(NettyHttpServer.class);
   private final HttpContentProcessorResolver processorResolver;
   private final ExecutorService executorService;

   public InputStreamBodyBinder(HttpContentProcessorResolver processorResolver, ExecutorService executorService) {
      this.processorResolver = processorResolver;
      this.executorService = executorService;
   }

   @Override
   public Argument<InputStream> argumentType() {
      return TYPE;
   }

   public ArgumentBinder.BindingResult<InputStream> bind(ArgumentConversionContext<InputStream> context, HttpRequest<?> source) {
      if (source instanceof NettyHttpRequest) {
         final NettyHttpRequest nettyHttpRequest = (NettyHttpRequest)source;
         io.netty.handler.codec.http.HttpRequest nativeRequest = nettyHttpRequest.getNativeRequest();
         if (nativeRequest instanceof StreamedHttpRequest) {
            final PipedOutputStream outputStream = new PipedOutputStream();

            try {
               PipedInputStream inputStream = new PipedInputStream(outputStream) {
                  private volatile HttpContentProcessor<ByteBufHolder> processor;

                  private synchronized void init() {
                     if (this.processor == null) {
                        this.processor = InputStreamBodyBinder.this.processorResolver.resolve(nettyHttpRequest, context.getArgument());
                        Flux.from(this.processor)
                           .publishOn(Schedulers.fromExecutor(InputStreamBodyBinder.this.executorService))
                           .subscribe(new CompletionAwareSubscriber<ByteBufHolder>() {
                              @Override
                              protected void doOnSubscribe(Subscription subscription) {
                                 subscription.request(1L);
                              }
   
                              protected synchronized void doOnNext(ByteBufHolder message) {
                                 if (InputStreamBodyBinder.LOG.isTraceEnabled()) {
                                    InputStreamBodyBinder.LOG.trace("Server received streaming message for argument [{}]: {}", context.getArgument(), message);
                                 }
   
                                 ByteBuf content = message.content();
                                 label45:
                                 if (!(content instanceof EmptyByteBuf)) {
                                    try {
                                       byte[] bytes = ByteBufUtil.getBytes(content);
                                       outputStream.write(bytes, 0, bytes.length);
                                       break label45;
                                    } catch (IOException var7) {
                                       this.subscription.cancel();
                                    } finally {
                                       content.release();
                                    }
   
                                    return;
                                 }
   
                                 this.subscription.request(1L);
                              }
   
                              @Override
                              protected synchronized void doOnError(Throwable t) {
                                 if (InputStreamBodyBinder.LOG.isTraceEnabled()) {
                                    InputStreamBodyBinder.LOG.trace("Server received error for argument [" + context.getArgument() + "]: " + t.getMessage(), t);
                                 }
   
                                 try {
                                    outputStream.close();
                                 } catch (IOException var6) {
                                 } finally {
                                    this.subscription.cancel();
                                 }
   
                              }
   
                              @Override
                              protected synchronized void doOnComplete() {
                                 if (InputStreamBodyBinder.LOG.isTraceEnabled()) {
                                    InputStreamBodyBinder.LOG.trace("Done receiving messages for argument: {}", context.getArgument());
                                 }
   
                                 try {
                                    outputStream.close();
                                 } catch (IOException var2) {
                                 }
   
                              }
                           });
                     }

                  }

                  public synchronized int read(byte[] b, int off, int len) throws IOException {
                     this.init();
                     return super.read(b, off, len);
                  }

                  public synchronized int read() throws IOException {
                     this.init();
                     return super.read();
                  }
               };
               return () -> Optional.of(inputStream);
            } catch (IOException var7) {
               context.reject(var7);
            }
         }
      }

      return ArgumentBinder.BindingResult.EMPTY;
   }
}
