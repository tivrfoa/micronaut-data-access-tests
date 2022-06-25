package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.processor.SingleThreadedBufferingProcessor;
import io.micronaut.core.async.subscriber.SingleThreadedBufferingSubscriber;
import io.micronaut.http.exceptions.ContentLengthExceededException;
import io.micronaut.http.netty.stream.StreamedHttpMessage;
import io.micronaut.http.server.HttpServerConfiguration;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscriber;

@Internal
public class DefaultHttpContentProcessor extends SingleThreadedBufferingProcessor<ByteBufHolder, ByteBufHolder> implements HttpContentProcessor<ByteBufHolder> {
   protected final NettyHttpRequest nettyHttpRequest;
   protected final ChannelHandlerContext ctx;
   protected final HttpServerConfiguration configuration;
   protected final long advertisedLength;
   protected final long requestMaxSize;
   protected final StreamedHttpMessage streamedHttpMessage;
   protected final AtomicLong receivedLength = new AtomicLong();

   public DefaultHttpContentProcessor(NettyHttpRequest<?> nettyHttpRequest, HttpServerConfiguration configuration) {
      this.nettyHttpRequest = nettyHttpRequest;
      HttpRequest nativeRequest = nettyHttpRequest.getNativeRequest();
      if (!(nativeRequest instanceof StreamedHttpMessage)) {
         throw new IllegalStateException("Streamed HTTP message expected");
      } else {
         this.streamedHttpMessage = (StreamedHttpMessage)nativeRequest;
         this.configuration = configuration;
         this.requestMaxSize = configuration.getMaxRequestSize();
         this.ctx = nettyHttpRequest.getChannelHandlerContext();
         this.advertisedLength = nettyHttpRequest.getContentLength();
      }
   }

   @Override
   public final void subscribe(Subscriber<? super ByteBufHolder> downstreamSubscriber) {
      super.subscribe(downstreamSubscriber);
      StreamedHttpMessage message = (StreamedHttpMessage)this.nettyHttpRequest.getNativeRequest();
      message.subscribe(this);
   }

   protected void onUpstreamMessage(ByteBufHolder message) {
      long receivedLength = this.receivedLength.addAndGet(this.resolveLength(message));
      if (this.advertisedLength > this.requestMaxSize) {
         this.fireExceedsLength(this.advertisedLength, this.requestMaxSize, message);
      } else if (receivedLength > this.requestMaxSize) {
         this.fireExceedsLength(receivedLength, this.requestMaxSize, message);
      } else {
         this.publishVerifiedContent(message);
      }

   }

   private long resolveLength(ByteBufHolder message) {
      return message instanceof HttpData ? ((HttpData)message).length() : (long)message.content().readableBytes();
   }

   private void fireExceedsLength(long receivedLength, long expected, ByteBufHolder message) {
      this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.DONE;
      this.upstreamSubscription.cancel();
      this.upstreamBuffer.clear();
      this.currentDownstreamSubscriber().ifPresent(subscriber -> subscriber.onError(new ContentLengthExceededException(expected, receivedLength)));
      ReferenceCountUtil.safeRelease(message);
   }

   private void publishVerifiedContent(ByteBufHolder message) {
      this.currentDownstreamSubscriber().ifPresent(subscriber -> subscriber.onNext(message));
   }
}
