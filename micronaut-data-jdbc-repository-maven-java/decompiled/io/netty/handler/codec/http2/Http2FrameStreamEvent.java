package io.netty.handler.codec.http2;

public final class Http2FrameStreamEvent {
   private final Http2FrameStream stream;
   private final Http2FrameStreamEvent.Type type;

   private Http2FrameStreamEvent(Http2FrameStream stream, Http2FrameStreamEvent.Type type) {
      this.stream = stream;
      this.type = type;
   }

   public Http2FrameStream stream() {
      return this.stream;
   }

   public Http2FrameStreamEvent.Type type() {
      return this.type;
   }

   static Http2FrameStreamEvent stateChanged(Http2FrameStream stream) {
      return new Http2FrameStreamEvent(stream, Http2FrameStreamEvent.Type.State);
   }

   static Http2FrameStreamEvent writabilityChanged(Http2FrameStream stream) {
      return new Http2FrameStreamEvent(stream, Http2FrameStreamEvent.Type.Writability);
   }

   public static enum Type {
      State,
      Writability;
   }
}
