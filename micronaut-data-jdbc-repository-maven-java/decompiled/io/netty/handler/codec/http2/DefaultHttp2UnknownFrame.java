package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;

public final class DefaultHttp2UnknownFrame extends DefaultByteBufHolder implements Http2UnknownFrame {
   private final byte frameType;
   private final Http2Flags flags;
   private Http2FrameStream stream;

   public DefaultHttp2UnknownFrame(byte frameType, Http2Flags flags) {
      this(frameType, flags, Unpooled.EMPTY_BUFFER);
   }

   public DefaultHttp2UnknownFrame(byte frameType, Http2Flags flags, ByteBuf data) {
      super(data);
      this.frameType = frameType;
      this.flags = flags;
   }

   @Override
   public Http2FrameStream stream() {
      return this.stream;
   }

   public DefaultHttp2UnknownFrame stream(Http2FrameStream stream) {
      this.stream = stream;
      return this;
   }

   @Override
   public byte frameType() {
      return this.frameType;
   }

   @Override
   public Http2Flags flags() {
      return this.flags;
   }

   @Override
   public String name() {
      return "UNKNOWN";
   }

   public DefaultHttp2UnknownFrame copy() {
      return this.replace(this.content().copy());
   }

   public DefaultHttp2UnknownFrame duplicate() {
      return this.replace(this.content().duplicate());
   }

   public DefaultHttp2UnknownFrame retainedDuplicate() {
      return this.replace(this.content().retainedDuplicate());
   }

   public DefaultHttp2UnknownFrame replace(ByteBuf content) {
      return new DefaultHttp2UnknownFrame(this.frameType, this.flags, content).stream(this.stream);
   }

   public DefaultHttp2UnknownFrame retain() {
      super.retain();
      return this;
   }

   public DefaultHttp2UnknownFrame retain(int increment) {
      super.retain(increment);
      return this;
   }

   @Override
   public String toString() {
      return StringUtil.simpleClassName(this)
         + "(frameType="
         + this.frameType
         + ", stream="
         + this.stream
         + ", flags="
         + this.flags
         + ", content="
         + this.contentToString()
         + ')';
   }

   public DefaultHttp2UnknownFrame touch() {
      super.touch();
      return this;
   }

   public DefaultHttp2UnknownFrame touch(Object hint) {
      super.touch(hint);
      return this;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof DefaultHttp2UnknownFrame)) {
         return false;
      } else {
         DefaultHttp2UnknownFrame other = (DefaultHttp2UnknownFrame)o;
         Http2FrameStream otherStream = other.stream();
         return (this.stream == otherStream || otherStream != null && otherStream.equals(this.stream))
            && this.flags.equals(other.flags())
            && this.frameType == other.frameType()
            && super.equals(other);
      }
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = hash * 31 + this.frameType;
      hash = hash * 31 + this.flags.hashCode();
      if (this.stream != null) {
         hash = hash * 31 + this.stream.hashCode();
      }

      return hash;
   }
}
