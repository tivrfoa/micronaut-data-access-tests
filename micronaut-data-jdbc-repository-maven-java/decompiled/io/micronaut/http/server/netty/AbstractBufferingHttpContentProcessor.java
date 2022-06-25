package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.processor.SingleThreadedBufferingProcessor;
import io.micronaut.http.exceptions.ContentLengthExceededException;
import io.micronaut.http.netty.stream.StreamedHttpMessage;
import io.micronaut.http.server.HttpServerConfiguration;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.http.multipart.HttpData;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscriber;

@Internal
public abstract class AbstractBufferingHttpContentProcessor<T> extends SingleThreadedBufferingProcessor<ByteBufHolder, T> implements HttpContentProcessor<T> {
   protected final NettyHttpRequest nettyHttpRequest;
   protected final long advertisedLength;
   protected final long requestMaxSize;
   protected final AtomicLong receivedLength = new AtomicLong();
   protected final HttpServerConfiguration configuration;
   private final long partMaxSize;

   public AbstractBufferingHttpContentProcessor(NettyHttpRequest<?> nettyHttpRequest, HttpServerConfiguration configuration) {
      this.nettyHttpRequest = nettyHttpRequest;
      this.advertisedLength = nettyHttpRequest.getContentLength();
      this.requestMaxSize = configuration.getMaxRequestSize();
      this.configuration = configuration;
      this.partMaxSize = configuration.getMultipart().getMaxFileSize();
   }

   @Override
   public void subscribe(Subscriber<? super T> downstreamSubscriber) {
      super.subscribe(downstreamSubscriber);
      this.subscribeUpstream();
   }

   protected final void doOnNext(ByteBufHolder message) {
      long receivedLength = this.receivedLength.addAndGet(this.resolveLength(message));
      if ((this.advertisedLength == -1L || receivedLength <= this.advertisedLength) && receivedLength <= this.requestMaxSize) {
         this.onUpstreamMessage(message);
      } else {
         this.fireExceedsLength(receivedLength, this.advertisedLength == -1L ? this.requestMaxSize : this.advertisedLength);
      }

   }

   protected boolean verifyPartDefinedSize(ByteBufHolder message) {
      long partLength = message instanceof HttpData ? ((HttpData)message).definedLength() : -1L;
      boolean validPart = partLength > this.partMaxSize;
      if (validPart) {
         this.fireExceedsLength(partLength, this.partMaxSize);
         return false;
      } else {
         return true;
      }
   }

   protected void fireExceedsLength(long receivedLength, long expected) {
      try {
         this.onError(new ContentLengthExceededException(expected, receivedLength));
      } finally {
         this.upstreamSubscription.cancel();
      }

   }

   private long resolveLength(ByteBufHolder message) {
      return message instanceof HttpData ? ((HttpData)message).length() : (long)message.content().readableBytes();
   }

   private void subscribeUpstream() {
      StreamedHttpMessage message = (StreamedHttpMessage)this.nettyHttpRequest.getNativeRequest();
      message.subscribe(this);
   }
}
