package io.netty.handler.codec.http2;

import io.netty.util.internal.ObjectUtil;

final class Http2EmptyDataFrameConnectionDecoder extends DecoratingHttp2ConnectionDecoder {
   private final int maxConsecutiveEmptyFrames;

   Http2EmptyDataFrameConnectionDecoder(Http2ConnectionDecoder delegate, int maxConsecutiveEmptyFrames) {
      super(delegate);
      this.maxConsecutiveEmptyFrames = ObjectUtil.checkPositive(maxConsecutiveEmptyFrames, "maxConsecutiveEmptyFrames");
   }

   @Override
   public void frameListener(Http2FrameListener listener) {
      if (listener != null) {
         super.frameListener(new Http2EmptyDataFrameListener(listener, this.maxConsecutiveEmptyFrames));
      } else {
         super.frameListener(null);
      }

   }

   @Override
   public Http2FrameListener frameListener() {
      Http2FrameListener frameListener = this.frameListener0();
      return frameListener instanceof Http2EmptyDataFrameListener ? ((Http2EmptyDataFrameListener)frameListener).listener : frameListener;
   }

   Http2FrameListener frameListener0() {
      return super.frameListener();
   }
}
