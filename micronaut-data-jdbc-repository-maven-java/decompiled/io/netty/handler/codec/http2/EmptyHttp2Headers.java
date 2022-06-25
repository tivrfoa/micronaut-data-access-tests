package io.netty.handler.codec.http2;

import io.netty.handler.codec.EmptyHeaders;

public final class EmptyHttp2Headers extends EmptyHeaders<CharSequence, CharSequence, Http2Headers> implements Http2Headers {
   public static final EmptyHttp2Headers INSTANCE = new EmptyHttp2Headers();

   private EmptyHttp2Headers() {
   }

   public EmptyHttp2Headers method(CharSequence method) {
      throw new UnsupportedOperationException();
   }

   public EmptyHttp2Headers scheme(CharSequence status) {
      throw new UnsupportedOperationException();
   }

   public EmptyHttp2Headers authority(CharSequence authority) {
      throw new UnsupportedOperationException();
   }

   public EmptyHttp2Headers path(CharSequence path) {
      throw new UnsupportedOperationException();
   }

   public EmptyHttp2Headers status(CharSequence status) {
      throw new UnsupportedOperationException();
   }

   @Override
   public CharSequence method() {
      return this.get(Http2Headers.PseudoHeaderName.METHOD.value());
   }

   @Override
   public CharSequence scheme() {
      return this.get(Http2Headers.PseudoHeaderName.SCHEME.value());
   }

   @Override
   public CharSequence authority() {
      return this.get(Http2Headers.PseudoHeaderName.AUTHORITY.value());
   }

   @Override
   public CharSequence path() {
      return this.get(Http2Headers.PseudoHeaderName.PATH.value());
   }

   @Override
   public CharSequence status() {
      return this.get(Http2Headers.PseudoHeaderName.STATUS.value());
   }

   @Override
   public boolean contains(CharSequence name, CharSequence value, boolean caseInsensitive) {
      return false;
   }
}
