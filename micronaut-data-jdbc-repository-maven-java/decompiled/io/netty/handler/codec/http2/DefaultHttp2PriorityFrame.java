package io.netty.handler.codec.http2;

public final class DefaultHttp2PriorityFrame extends AbstractHttp2StreamFrame implements Http2PriorityFrame {
   private final int streamDependency;
   private final short weight;
   private final boolean exclusive;

   public DefaultHttp2PriorityFrame(int streamDependency, short weight, boolean exclusive) {
      this.streamDependency = streamDependency;
      this.weight = weight;
      this.exclusive = exclusive;
   }

   @Override
   public int streamDependency() {
      return this.streamDependency;
   }

   @Override
   public short weight() {
      return this.weight;
   }

   @Override
   public boolean exclusive() {
      return this.exclusive;
   }

   public DefaultHttp2PriorityFrame stream(Http2FrameStream stream) {
      super.stream(stream);
      return this;
   }

   @Override
   public String name() {
      return "PRIORITY_FRAME";
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof DefaultHttp2PriorityFrame)) {
         return false;
      } else {
         DefaultHttp2PriorityFrame other = (DefaultHttp2PriorityFrame)o;
         boolean same = super.equals(other);
         return same && this.streamDependency == other.streamDependency && this.weight == other.weight && this.exclusive == other.exclusive;
      }
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = hash * 31 + this.streamDependency;
      hash = hash * 31 + this.weight;
      return hash * 31 + (this.exclusive ? 1 : 0);
   }

   public String toString() {
      return "DefaultHttp2PriorityFrame(stream="
         + this.stream()
         + ", streamDependency="
         + this.streamDependency
         + ", weight="
         + this.weight
         + ", exclusive="
         + this.exclusive
         + ')';
   }
}
