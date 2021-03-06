package io.netty.buffer;

import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ByteBufOutputStream extends OutputStream implements DataOutput {
   private final ByteBuf buffer;
   private final int startIndex;
   private DataOutputStream utf8out;
   private boolean closed;

   public ByteBufOutputStream(ByteBuf buffer) {
      this.buffer = ObjectUtil.checkNotNull(buffer, "buffer");
      this.startIndex = buffer.writerIndex();
   }

   public int writtenBytes() {
      return this.buffer.writerIndex() - this.startIndex;
   }

   public void write(byte[] b, int off, int len) throws IOException {
      if (len != 0) {
         this.buffer.writeBytes(b, off, len);
      }
   }

   public void write(byte[] b) throws IOException {
      this.buffer.writeBytes(b);
   }

   public void write(int b) throws IOException {
      this.buffer.writeByte(b);
   }

   public void writeBoolean(boolean v) throws IOException {
      this.buffer.writeBoolean(v);
   }

   public void writeByte(int v) throws IOException {
      this.buffer.writeByte(v);
   }

   public void writeBytes(String s) throws IOException {
      this.buffer.writeCharSequence(s, CharsetUtil.US_ASCII);
   }

   public void writeChar(int v) throws IOException {
      this.buffer.writeChar(v);
   }

   public void writeChars(String s) throws IOException {
      int len = s.length();

      for(int i = 0; i < len; ++i) {
         this.buffer.writeChar(s.charAt(i));
      }

   }

   public void writeDouble(double v) throws IOException {
      this.buffer.writeDouble(v);
   }

   public void writeFloat(float v) throws IOException {
      this.buffer.writeFloat(v);
   }

   public void writeInt(int v) throws IOException {
      this.buffer.writeInt(v);
   }

   public void writeLong(long v) throws IOException {
      this.buffer.writeLong(v);
   }

   public void writeShort(int v) throws IOException {
      this.buffer.writeShort((short)v);
   }

   public void writeUTF(String s) throws IOException {
      DataOutputStream out = this.utf8out;
      if (out == null) {
         if (this.closed) {
            throw new IOException("The stream is closed");
         }

         this.utf8out = out = new DataOutputStream(this);
      }

      out.writeUTF(s);
   }

   public ByteBuf buffer() {
      return this.buffer;
   }

   public void close() throws IOException {
      if (!this.closed) {
         this.closed = true;

         try {
            super.close();
         } finally {
            if (this.utf8out != null) {
               this.utf8out.close();
            }

         }

      }
   }
}
