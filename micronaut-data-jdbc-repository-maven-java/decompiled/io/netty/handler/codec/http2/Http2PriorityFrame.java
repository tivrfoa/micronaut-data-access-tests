package io.netty.handler.codec.http2;

public interface Http2PriorityFrame extends Http2StreamFrame {
   int streamDependency();

   short weight();

   boolean exclusive();

   Http2PriorityFrame stream(Http2FrameStream var1);
}
