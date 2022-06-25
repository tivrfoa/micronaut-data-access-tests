package io.netty.handler.codec.socksx;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;

public abstract class AbstractSocksMessage implements SocksMessage {
   private DecoderResult decoderResult = DecoderResult.SUCCESS;

   @Override
   public DecoderResult decoderResult() {
      return this.decoderResult;
   }

   @Override
   public void setDecoderResult(DecoderResult decoderResult) {
      this.decoderResult = ObjectUtil.checkNotNull(decoderResult, "decoderResult");
   }
}
