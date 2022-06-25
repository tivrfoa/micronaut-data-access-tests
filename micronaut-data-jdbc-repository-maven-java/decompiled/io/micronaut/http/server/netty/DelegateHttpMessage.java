package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpVersion;

@Internal
class DelegateHttpMessage implements HttpMessage {
   protected final HttpMessage message;

   DelegateHttpMessage(HttpMessage message) {
      this.message = message;
   }

   @Deprecated
   @Override
   public HttpVersion getProtocolVersion() {
      return this.message.protocolVersion();
   }

   @Override
   public HttpVersion protocolVersion() {
      return this.message.protocolVersion();
   }

   @Override
   public HttpMessage setProtocolVersion(HttpVersion version) {
      this.message.setProtocolVersion(version);
      return this;
   }

   @Override
   public HttpHeaders headers() {
      return this.message.headers();
   }

   @Deprecated
   @Override
   public DecoderResult getDecoderResult() {
      return this.message.decoderResult();
   }

   @Override
   public DecoderResult decoderResult() {
      return this.message.decoderResult();
   }

   @Override
   public void setDecoderResult(DecoderResult result) {
      this.message.setDecoderResult(result);
   }

   public String toString() {
      return this.getClass().getName() + "(" + this.message.toString() + ")";
   }
}
