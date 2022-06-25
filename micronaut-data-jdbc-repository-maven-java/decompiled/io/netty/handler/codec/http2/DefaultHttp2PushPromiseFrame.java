package io.netty.handler.codec.http2;

public final class DefaultHttp2PushPromiseFrame implements Http2PushPromiseFrame {
   private Http2FrameStream pushStreamFrame;
   private final Http2Headers http2Headers;
   private Http2FrameStream streamFrame;
   private final int padding;
   private final int promisedStreamId;

   public DefaultHttp2PushPromiseFrame(Http2Headers http2Headers) {
      this(http2Headers, 0);
   }

   public DefaultHttp2PushPromiseFrame(Http2Headers http2Headers, int padding) {
      this(http2Headers, padding, -1);
   }

   DefaultHttp2PushPromiseFrame(Http2Headers http2Headers, int padding, int promisedStreamId) {
      this.http2Headers = http2Headers;
      this.padding = padding;
      this.promisedStreamId = promisedStreamId;
   }

   @Override
   public Http2StreamFrame pushStream(Http2FrameStream stream) {
      this.pushStreamFrame = stream;
      return this;
   }

   @Override
   public Http2FrameStream pushStream() {
      return this.pushStreamFrame;
   }

   @Override
   public Http2Headers http2Headers() {
      return this.http2Headers;
   }

   @Override
   public int padding() {
      return this.padding;
   }

   @Override
   public int promisedStreamId() {
      return this.pushStreamFrame != null ? this.pushStreamFrame.id() : this.promisedStreamId;
   }

   @Override
   public Http2PushPromiseFrame stream(Http2FrameStream stream) {
      this.streamFrame = stream;
      return this;
   }

   @Override
   public Http2FrameStream stream() {
      return this.streamFrame;
   }

   @Override
   public String name() {
      return "PUSH_PROMISE_FRAME";
   }

   public String toString() {
      return "DefaultHttp2PushPromiseFrame{pushStreamFrame="
         + this.pushStreamFrame
         + ", http2Headers="
         + this.http2Headers
         + ", streamFrame="
         + this.streamFrame
         + ", padding="
         + this.padding
         + '}';
   }
}
