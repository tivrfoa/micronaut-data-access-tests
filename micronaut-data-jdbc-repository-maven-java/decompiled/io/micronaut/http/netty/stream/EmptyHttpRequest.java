package io.micronaut.http.netty.stream;

import io.micronaut.core.annotation.Internal;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;

@Internal
class EmptyHttpRequest extends DelegateHttpRequest implements FullHttpRequest {
   EmptyHttpRequest(HttpRequest request) {
      super(request);
   }

   @Override
   public FullHttpRequest setUri(String uri) {
      super.setUri(uri);
      return this;
   }

   @Override
   public FullHttpRequest setMethod(HttpMethod method) {
      super.setMethod(method);
      return this;
   }

   @Override
   public FullHttpRequest setProtocolVersion(HttpVersion version) {
      super.setProtocolVersion(version);
      return this;
   }

   @Override
   public FullHttpRequest copy() {
      if (this.request instanceof FullHttpRequest) {
         return new EmptyHttpRequest(((FullHttpRequest)this.request).copy());
      } else {
         DefaultHttpRequest copy = new DefaultHttpRequest(this.protocolVersion(), this.method(), this.uri());
         copy.headers().set(this.headers());
         return new EmptyHttpRequest(copy);
      }
   }

   @Override
   public FullHttpRequest retain(int increment) {
      ReferenceCountUtil.retain(this.message, increment);
      return this;
   }

   @Override
   public FullHttpRequest retain() {
      ReferenceCountUtil.retain(this.message);
      return this;
   }

   @Override
   public FullHttpRequest touch() {
      return (FullHttpRequest)(this.request instanceof FullHttpRequest ? ((FullHttpRequest)this.request).touch() : this);
   }

   @Override
   public FullHttpRequest touch(Object o) {
      return (FullHttpRequest)(this.request instanceof FullHttpRequest ? ((FullHttpRequest)this.request).touch(o) : this);
   }

   @Override
   public HttpHeaders trailingHeaders() {
      return new DefaultHttpHeaders();
   }

   @Override
   public FullHttpRequest duplicate() {
      return (FullHttpRequest)(this.request instanceof FullHttpRequest ? ((FullHttpRequest)this.request).duplicate() : this);
   }

   @Override
   public FullHttpRequest retainedDuplicate() {
      return (FullHttpRequest)(this.request instanceof FullHttpRequest ? ((FullHttpRequest)this.request).retainedDuplicate() : this);
   }

   @Override
   public FullHttpRequest replace(ByteBuf byteBuf) {
      return (FullHttpRequest)(this.message instanceof FullHttpRequest ? ((FullHttpRequest)this.request).replace(byteBuf) : this);
   }

   @Override
   public ByteBuf content() {
      return Unpooled.EMPTY_BUFFER;
   }

   @Override
   public int refCnt() {
      return this.message instanceof ReferenceCounted ? ((ReferenceCounted)this.message).refCnt() : 1;
   }

   @Override
   public boolean release() {
      return ReferenceCountUtil.release(this.message);
   }

   @Override
   public boolean release(int decrement) {
      return ReferenceCountUtil.release(this.message, decrement);
   }
}
