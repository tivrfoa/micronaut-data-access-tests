package io.micronaut.http.netty.stream;

import io.micronaut.core.annotation.Internal;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;

@Internal
class EmptyHttpResponse extends DelegateHttpResponse implements FullHttpResponse {
   EmptyHttpResponse(HttpResponse response) {
      super(response);
   }

   @Override
   public FullHttpResponse setStatus(HttpResponseStatus status) {
      super.setStatus(status);
      return this;
   }

   @Override
   public FullHttpResponse setProtocolVersion(HttpVersion version) {
      super.setProtocolVersion(version);
      return this;
   }

   @Override
   public FullHttpResponse copy() {
      if (this.response instanceof FullHttpResponse) {
         return new EmptyHttpResponse(((FullHttpResponse)this.response).copy());
      } else {
         DefaultHttpResponse copy = new DefaultHttpResponse(this.protocolVersion(), this.status());
         copy.headers().set(this.headers());
         return new EmptyHttpResponse(copy);
      }
   }

   @Override
   public FullHttpResponse retain(int increment) {
      ReferenceCountUtil.retain(this.message, increment);
      return this;
   }

   @Override
   public FullHttpResponse retain() {
      ReferenceCountUtil.retain(this.message);
      return this;
   }

   @Override
   public FullHttpResponse touch() {
      return (FullHttpResponse)(this.response instanceof FullHttpResponse ? ((FullHttpResponse)this.response).touch() : this);
   }

   @Override
   public FullHttpResponse touch(Object o) {
      return (FullHttpResponse)(this.response instanceof FullHttpResponse ? ((FullHttpResponse)this.response).touch(o) : this);
   }

   @Override
   public HttpHeaders trailingHeaders() {
      return new DefaultHttpHeaders();
   }

   @Override
   public FullHttpResponse duplicate() {
      return (FullHttpResponse)(this.response instanceof FullHttpResponse ? ((FullHttpResponse)this.response).duplicate() : this);
   }

   @Override
   public FullHttpResponse retainedDuplicate() {
      return (FullHttpResponse)(this.response instanceof FullHttpResponse ? ((FullHttpResponse)this.response).retainedDuplicate() : this);
   }

   @Override
   public FullHttpResponse replace(ByteBuf byteBuf) {
      return (FullHttpResponse)(this.response instanceof FullHttpResponse ? ((FullHttpResponse)this.response).replace(byteBuf) : this);
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
