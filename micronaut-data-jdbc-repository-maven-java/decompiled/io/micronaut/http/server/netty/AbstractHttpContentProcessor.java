package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.processor.SingleSubscriberProcessor;
import io.micronaut.http.exceptions.ContentLengthExceededException;
import io.micronaut.http.netty.stream.StreamedHttpMessage;
import io.micronaut.http.server.HttpServerConfiguration;
import io.netty.buffer.ByteBufHolder;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscriber;

@Internal
public abstract class AbstractHttpContentProcessor<T> extends SingleSubscriberProcessor<ByteBufHolder, T> implements HttpContentProcessor<T> {
   protected final NettyHttpRequest<?> nettyHttpRequest;
   protected final long advertisedLength;
   protected final long requestMaxSize;
   protected final AtomicLong receivedLength = new AtomicLong();
   protected final HttpServerConfiguration configuration;

   public AbstractHttpContentProcessor(NettyHttpRequest<?> nettyHttpRequest, HttpServerConfiguration configuration) {
      this.nettyHttpRequest = nettyHttpRequest;
      this.advertisedLength = nettyHttpRequest.getContentLength();
      this.requestMaxSize = configuration.getMaxRequestSize();
      this.configuration = configuration;
   }

   protected abstract void onData(ByteBufHolder message);

   @Override
   protected final void doSubscribe(Subscriber<? super T> subscriber) {
      StreamedHttpMessage message = (StreamedHttpMessage)this.nettyHttpRequest.getNativeRequest();
      message.subscribe(this);
   }

   protected final void doOnNext(ByteBufHolder message) {
      long receivedLength = this.receivedLength.addAndGet((long)message.content().readableBytes());
      ReferenceCountUtil.touch(message);
      if (this.advertisedLength > this.requestMaxSize) {
         this.fireExceedsLength(this.advertisedLength, this.requestMaxSize, message);
      } else if (receivedLength > this.requestMaxSize) {
         this.fireExceedsLength(receivedLength, this.requestMaxSize, message);
      } else {
         this.onData(message);
      }

   }

   protected void fireExceedsLength(long receivedLength, long expected, ByteBufHolder message) {
      try {
         this.onError(new ContentLengthExceededException(expected, receivedLength));
      } finally {
         ReferenceCountUtil.safeRelease(message);
         this.parentSubscription.cancel();
      }

   }
}
