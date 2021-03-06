package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.ObjectUtil;
import java.util.Collections;
import java.util.List;

public final class SocksInitRequest extends SocksRequest {
   private final List<SocksAuthScheme> authSchemes;

   public SocksInitRequest(List<SocksAuthScheme> authSchemes) {
      super(SocksRequestType.INIT);
      this.authSchemes = ObjectUtil.checkNotNull(authSchemes, "authSchemes");
   }

   public List<SocksAuthScheme> authSchemes() {
      return Collections.unmodifiableList(this.authSchemes);
   }

   @Override
   public void encodeAsByteBuf(ByteBuf byteBuf) {
      byteBuf.writeByte(this.protocolVersion().byteValue());
      byteBuf.writeByte(this.authSchemes.size());

      for(SocksAuthScheme authScheme : this.authSchemes) {
         byteBuf.writeByte(authScheme.byteValue());
      }

   }
}
