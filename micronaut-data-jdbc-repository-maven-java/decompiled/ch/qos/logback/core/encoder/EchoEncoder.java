package ch.qos.logback.core.encoder;

import ch.qos.logback.core.CoreConstants;

public class EchoEncoder<E> extends EncoderBase<E> {
   String fileHeader;
   String fileFooter;

   @Override
   public byte[] encode(E event) {
      String val = event + CoreConstants.LINE_SEPARATOR;
      return val.getBytes();
   }

   @Override
   public byte[] footerBytes() {
      return this.fileFooter == null ? null : this.fileFooter.getBytes();
   }

   @Override
   public byte[] headerBytes() {
      return this.fileHeader == null ? null : this.fileHeader.getBytes();
   }
}
