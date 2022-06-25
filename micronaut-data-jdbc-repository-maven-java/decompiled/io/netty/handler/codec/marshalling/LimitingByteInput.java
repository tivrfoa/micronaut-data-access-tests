package io.netty.handler.codec.marshalling;

import io.netty.util.internal.ObjectUtil;
import java.io.IOException;
import org.jboss.marshalling.ByteInput;

class LimitingByteInput implements ByteInput {
   private static final LimitingByteInput.TooBigObjectException EXCEPTION = new LimitingByteInput.TooBigObjectException();
   private final ByteInput input;
   private final long limit;
   private long read;

   LimitingByteInput(ByteInput input, long limit) {
      this.input = input;
      this.limit = ObjectUtil.checkPositive(limit, "limit");
   }

   public void close() throws IOException {
   }

   public int available() throws IOException {
      return this.readable(this.input.available());
   }

   public int read() throws IOException {
      int readable = this.readable(1);
      if (readable > 0) {
         int b = this.input.read();
         ++this.read;
         return b;
      } else {
         throw EXCEPTION;
      }
   }

   public int read(byte[] array) throws IOException {
      return this.read(array, 0, array.length);
   }

   public int read(byte[] array, int offset, int length) throws IOException {
      int readable = this.readable(length);
      if (readable > 0) {
         int i = this.input.read(array, offset, readable);
         this.read += (long)i;
         return i;
      } else {
         throw EXCEPTION;
      }
   }

   public long skip(long bytes) throws IOException {
      int readable = this.readable((int)bytes);
      if (readable > 0) {
         long i = this.input.skip((long)readable);
         this.read += i;
         return i;
      } else {
         throw EXCEPTION;
      }
   }

   private int readable(int length) {
      return (int)Math.min((long)length, this.limit - this.read);
   }

   static final class TooBigObjectException extends IOException {
      private static final long serialVersionUID = 1L;
   }
}
