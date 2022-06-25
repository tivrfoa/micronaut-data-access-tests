package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

public final class DefaultHttp2DataFrame extends AbstractHttp2StreamFrame implements Http2DataFrame {
   private final ByteBuf content;
   private final boolean endStream;
   private final int padding;
   private final int initialFlowControlledBytes;

   public DefaultHttp2DataFrame(ByteBuf content) {
      this(content, false);
   }

   public DefaultHttp2DataFrame(boolean endStream) {
      this(Unpooled.EMPTY_BUFFER, endStream);
   }

   public DefaultHttp2DataFrame(ByteBuf content, boolean endStream) {
      this(content, endStream, 0);
   }

   public DefaultHttp2DataFrame(ByteBuf content, boolean endStream, int padding) {
      this.content = ObjectUtil.checkNotNull(content, "content");
      this.endStream = endStream;
      Http2CodecUtil.verifyPadding(padding);
      this.padding = padding;
      if ((long)this.content().readableBytes() + (long)padding > 2147483647L) {
         throw new IllegalArgumentException("content + padding must be <= Integer.MAX_VALUE");
      } else {
         this.initialFlowControlledBytes = this.content().readableBytes() + padding;
      }
   }

   public DefaultHttp2DataFrame stream(Http2FrameStream stream) {
      super.stream(stream);
      return this;
   }

   @Override
   public String name() {
      return "DATA";
   }

   @Override
   public boolean isEndStream() {
      return this.endStream;
   }

   @Override
   public int padding() {
      return this.padding;
   }

   @Override
   public ByteBuf content() {
      return ByteBufUtil.ensureAccessible(this.content);
   }

   @Override
   public int initialFlowControlledBytes() {
      return this.initialFlowControlledBytes;
   }

   public DefaultHttp2DataFrame copy() {
      return this.replace(this.content().copy());
   }

   public DefaultHttp2DataFrame duplicate() {
      return this.replace(this.content().duplicate());
   }

   public DefaultHttp2DataFrame retainedDuplicate() {
      return this.replace(this.content().retainedDuplicate());
   }

   public DefaultHttp2DataFrame replace(ByteBuf content) {
      return new DefaultHttp2DataFrame(content, this.endStream, this.padding);
   }

   @Override
   public int refCnt() {
      return this.content.refCnt();
   }

   @Override
   public boolean release() {
      return this.content.release();
   }

   @Override
   public boolean release(int decrement) {
      return this.content.release(decrement);
   }

   public DefaultHttp2DataFrame retain() {
      this.content.retain();
      return this;
   }

   public DefaultHttp2DataFrame retain(int increment) {
      this.content.retain(increment);
      return this;
   }

   public String toString() {
      return StringUtil.simpleClassName(this)
         + "(stream="
         + this.stream()
         + ", content="
         + this.content
         + ", endStream="
         + this.endStream
         + ", padding="
         + this.padding
         + ')';
   }

   public DefaultHttp2DataFrame touch() {
      this.content.touch();
      return this;
   }

   public DefaultHttp2DataFrame touch(Object hint) {
      this.content.touch(hint);
      return this;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof DefaultHttp2DataFrame)) {
         return false;
      } else {
         DefaultHttp2DataFrame other = (DefaultHttp2DataFrame)o;
         return super.equals(other) && this.content.equals(other.content()) && this.endStream == other.endStream && this.padding == other.padding;
      }
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = hash * 31 + this.content.hashCode();
      hash = hash * 31 + (this.endStream ? 0 : 1);
      return hash * 31 + this.padding;
   }
}
