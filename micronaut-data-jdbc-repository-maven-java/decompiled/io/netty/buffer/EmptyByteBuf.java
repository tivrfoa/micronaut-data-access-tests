package io.netty.buffer;

import io.netty.util.ByteProcessor;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

public final class EmptyByteBuf extends ByteBuf {
   static final int EMPTY_BYTE_BUF_HASH_CODE = 1;
   private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocateDirect(0);
   private static final long EMPTY_BYTE_BUFFER_ADDRESS;
   private final ByteBufAllocator alloc;
   private final ByteOrder order;
   private final String str;
   private EmptyByteBuf swapped;

   public EmptyByteBuf(ByteBufAllocator alloc) {
      this(alloc, ByteOrder.BIG_ENDIAN);
   }

   private EmptyByteBuf(ByteBufAllocator alloc, ByteOrder order) {
      this.alloc = ObjectUtil.checkNotNull(alloc, "alloc");
      this.order = order;
      this.str = StringUtil.simpleClassName(this) + (order == ByteOrder.BIG_ENDIAN ? "BE" : "LE");
   }

   @Override
   public int capacity() {
      return 0;
   }

   @Override
   public ByteBuf capacity(int newCapacity) {
      throw new ReadOnlyBufferException();
   }

   @Override
   public ByteBufAllocator alloc() {
      return this.alloc;
   }

   @Override
   public ByteOrder order() {
      return this.order;
   }

   @Override
   public ByteBuf unwrap() {
      return null;
   }

   @Override
   public ByteBuf asReadOnly() {
      return Unpooled.unmodifiableBuffer((ByteBuf)this);
   }

   @Override
   public boolean isReadOnly() {
      return false;
   }

   @Override
   public boolean isDirect() {
      return true;
   }

   @Override
   public int maxCapacity() {
      return 0;
   }

   @Override
   public ByteBuf order(ByteOrder endianness) {
      if (ObjectUtil.checkNotNull(endianness, "endianness") == this.order()) {
         return this;
      } else {
         EmptyByteBuf swapped = this.swapped;
         if (swapped != null) {
            return swapped;
         } else {
            this.swapped = swapped = new EmptyByteBuf(this.alloc(), endianness);
            return swapped;
         }
      }
   }

   @Override
   public int readerIndex() {
      return 0;
   }

   @Override
   public ByteBuf readerIndex(int readerIndex) {
      return this.checkIndex(readerIndex);
   }

   @Override
   public int writerIndex() {
      return 0;
   }

   @Override
   public ByteBuf writerIndex(int writerIndex) {
      return this.checkIndex(writerIndex);
   }

   @Override
   public ByteBuf setIndex(int readerIndex, int writerIndex) {
      this.checkIndex(readerIndex);
      this.checkIndex(writerIndex);
      return this;
   }

   @Override
   public int readableBytes() {
      return 0;
   }

   @Override
   public int writableBytes() {
      return 0;
   }

   @Override
   public int maxWritableBytes() {
      return 0;
   }

   @Override
   public boolean isReadable() {
      return false;
   }

   @Override
   public boolean isWritable() {
      return false;
   }

   @Override
   public ByteBuf clear() {
      return this;
   }

   @Override
   public ByteBuf markReaderIndex() {
      return this;
   }

   @Override
   public ByteBuf resetReaderIndex() {
      return this;
   }

   @Override
   public ByteBuf markWriterIndex() {
      return this;
   }

   @Override
   public ByteBuf resetWriterIndex() {
      return this;
   }

   @Override
   public ByteBuf discardReadBytes() {
      return this;
   }

   @Override
   public ByteBuf discardSomeReadBytes() {
      return this;
   }

   @Override
   public ByteBuf ensureWritable(int minWritableBytes) {
      ObjectUtil.checkPositiveOrZero(minWritableBytes, "minWritableBytes");
      if (minWritableBytes != 0) {
         throw new IndexOutOfBoundsException();
      } else {
         return this;
      }
   }

   @Override
   public int ensureWritable(int minWritableBytes, boolean force) {
      ObjectUtil.checkPositiveOrZero(minWritableBytes, "minWritableBytes");
      return minWritableBytes == 0 ? 0 : 1;
   }

   @Override
   public boolean getBoolean(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public byte getByte(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public short getUnsignedByte(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public short getShort(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public short getShortLE(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int getUnsignedShort(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int getUnsignedShortLE(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int getMedium(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int getMediumLE(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int getUnsignedMedium(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int getUnsignedMediumLE(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int getInt(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int getIntLE(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public long getUnsignedInt(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public long getUnsignedIntLE(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public long getLong(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public long getLongLE(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public char getChar(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public float getFloat(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public double getDouble(int index) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf getBytes(int index, ByteBuf dst) {
      return this.checkIndex(index, dst.writableBytes());
   }

   @Override
   public ByteBuf getBytes(int index, ByteBuf dst, int length) {
      return this.checkIndex(index, length);
   }

   @Override
   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
      return this.checkIndex(index, length);
   }

   @Override
   public ByteBuf getBytes(int index, byte[] dst) {
      return this.checkIndex(index, dst.length);
   }

   @Override
   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
      return this.checkIndex(index, length);
   }

   @Override
   public ByteBuf getBytes(int index, ByteBuffer dst) {
      return this.checkIndex(index, dst.remaining());
   }

   @Override
   public ByteBuf getBytes(int index, OutputStream out, int length) {
      return this.checkIndex(index, length);
   }

   @Override
   public int getBytes(int index, GatheringByteChannel out, int length) {
      this.checkIndex(index, length);
      return 0;
   }

   @Override
   public int getBytes(int index, FileChannel out, long position, int length) {
      this.checkIndex(index, length);
      return 0;
   }

   @Override
   public CharSequence getCharSequence(int index, int length, Charset charset) {
      this.checkIndex(index, length);
      return null;
   }

   @Override
   public ByteBuf setBoolean(int index, boolean value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setByte(int index, int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setShort(int index, int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setShortLE(int index, int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setMedium(int index, int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setMediumLE(int index, int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setInt(int index, int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setIntLE(int index, int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setLong(int index, long value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setLongLE(int index, long value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setChar(int index, int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setFloat(int index, float value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setDouble(int index, double value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setBytes(int index, ByteBuf src) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf setBytes(int index, ByteBuf src, int length) {
      return this.checkIndex(index, length);
   }

   @Override
   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
      return this.checkIndex(index, length);
   }

   @Override
   public ByteBuf setBytes(int index, byte[] src) {
      return this.checkIndex(index, src.length);
   }

   @Override
   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
      return this.checkIndex(index, length);
   }

   @Override
   public ByteBuf setBytes(int index, ByteBuffer src) {
      return this.checkIndex(index, src.remaining());
   }

   @Override
   public int setBytes(int index, InputStream in, int length) {
      this.checkIndex(index, length);
      return 0;
   }

   @Override
   public int setBytes(int index, ScatteringByteChannel in, int length) {
      this.checkIndex(index, length);
      return 0;
   }

   @Override
   public int setBytes(int index, FileChannel in, long position, int length) {
      this.checkIndex(index, length);
      return 0;
   }

   @Override
   public ByteBuf setZero(int index, int length) {
      return this.checkIndex(index, length);
   }

   @Override
   public int setCharSequence(int index, CharSequence sequence, Charset charset) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public boolean readBoolean() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public byte readByte() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public short readUnsignedByte() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public short readShort() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public short readShortLE() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int readUnsignedShort() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int readUnsignedShortLE() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int readMedium() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int readMediumLE() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int readUnsignedMedium() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int readUnsignedMediumLE() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int readInt() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int readIntLE() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public long readUnsignedInt() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public long readUnsignedIntLE() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public long readLong() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public long readLongLE() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public char readChar() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public float readFloat() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public double readDouble() {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf readBytes(int length) {
      return this.checkLength(length);
   }

   @Override
   public ByteBuf readSlice(int length) {
      return this.checkLength(length);
   }

   @Override
   public ByteBuf readRetainedSlice(int length) {
      return this.checkLength(length);
   }

   @Override
   public ByteBuf readBytes(ByteBuf dst) {
      return this.checkLength(dst.writableBytes());
   }

   @Override
   public ByteBuf readBytes(ByteBuf dst, int length) {
      return this.checkLength(length);
   }

   @Override
   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
      return this.checkLength(length);
   }

   @Override
   public ByteBuf readBytes(byte[] dst) {
      return this.checkLength(dst.length);
   }

   @Override
   public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
      return this.checkLength(length);
   }

   @Override
   public ByteBuf readBytes(ByteBuffer dst) {
      return this.checkLength(dst.remaining());
   }

   @Override
   public ByteBuf readBytes(OutputStream out, int length) {
      return this.checkLength(length);
   }

   @Override
   public int readBytes(GatheringByteChannel out, int length) {
      this.checkLength(length);
      return 0;
   }

   @Override
   public int readBytes(FileChannel out, long position, int length) {
      this.checkLength(length);
      return 0;
   }

   @Override
   public CharSequence readCharSequence(int length, Charset charset) {
      this.checkLength(length);
      return "";
   }

   @Override
   public ByteBuf skipBytes(int length) {
      return this.checkLength(length);
   }

   @Override
   public ByteBuf writeBoolean(boolean value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeByte(int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeShort(int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeShortLE(int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeMedium(int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeMediumLE(int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeInt(int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeIntLE(int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeLong(long value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeLongLE(long value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeChar(int value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeFloat(float value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeDouble(double value) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public ByteBuf writeBytes(ByteBuf src) {
      return this.checkLength(src.readableBytes());
   }

   @Override
   public ByteBuf writeBytes(ByteBuf src, int length) {
      return this.checkLength(length);
   }

   @Override
   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
      return this.checkLength(length);
   }

   @Override
   public ByteBuf writeBytes(byte[] src) {
      return this.checkLength(src.length);
   }

   @Override
   public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
      return this.checkLength(length);
   }

   @Override
   public ByteBuf writeBytes(ByteBuffer src) {
      return this.checkLength(src.remaining());
   }

   @Override
   public int writeBytes(InputStream in, int length) {
      this.checkLength(length);
      return 0;
   }

   @Override
   public int writeBytes(ScatteringByteChannel in, int length) {
      this.checkLength(length);
      return 0;
   }

   @Override
   public int writeBytes(FileChannel in, long position, int length) {
      this.checkLength(length);
      return 0;
   }

   @Override
   public ByteBuf writeZero(int length) {
      return this.checkLength(length);
   }

   @Override
   public int writeCharSequence(CharSequence sequence, Charset charset) {
      throw new IndexOutOfBoundsException();
   }

   @Override
   public int indexOf(int fromIndex, int toIndex, byte value) {
      this.checkIndex(fromIndex);
      this.checkIndex(toIndex);
      return -1;
   }

   @Override
   public int bytesBefore(byte value) {
      return -1;
   }

   @Override
   public int bytesBefore(int length, byte value) {
      this.checkLength(length);
      return -1;
   }

   @Override
   public int bytesBefore(int index, int length, byte value) {
      this.checkIndex(index, length);
      return -1;
   }

   @Override
   public int forEachByte(ByteProcessor processor) {
      return -1;
   }

   @Override
   public int forEachByte(int index, int length, ByteProcessor processor) {
      this.checkIndex(index, length);
      return -1;
   }

   @Override
   public int forEachByteDesc(ByteProcessor processor) {
      return -1;
   }

   @Override
   public int forEachByteDesc(int index, int length, ByteProcessor processor) {
      this.checkIndex(index, length);
      return -1;
   }

   @Override
   public ByteBuf copy() {
      return this;
   }

   @Override
   public ByteBuf copy(int index, int length) {
      return this.checkIndex(index, length);
   }

   @Override
   public ByteBuf slice() {
      return this;
   }

   @Override
   public ByteBuf retainedSlice() {
      return this;
   }

   @Override
   public ByteBuf slice(int index, int length) {
      return this.checkIndex(index, length);
   }

   @Override
   public ByteBuf retainedSlice(int index, int length) {
      return this.checkIndex(index, length);
   }

   @Override
   public ByteBuf duplicate() {
      return this;
   }

   @Override
   public ByteBuf retainedDuplicate() {
      return this;
   }

   @Override
   public int nioBufferCount() {
      return 1;
   }

   @Override
   public ByteBuffer nioBuffer() {
      return EMPTY_BYTE_BUFFER;
   }

   @Override
   public ByteBuffer nioBuffer(int index, int length) {
      this.checkIndex(index, length);
      return this.nioBuffer();
   }

   @Override
   public ByteBuffer[] nioBuffers() {
      return new ByteBuffer[]{EMPTY_BYTE_BUFFER};
   }

   @Override
   public ByteBuffer[] nioBuffers(int index, int length) {
      this.checkIndex(index, length);
      return this.nioBuffers();
   }

   @Override
   public ByteBuffer internalNioBuffer(int index, int length) {
      return EMPTY_BYTE_BUFFER;
   }

   @Override
   public boolean hasArray() {
      return true;
   }

   @Override
   public byte[] array() {
      return EmptyArrays.EMPTY_BYTES;
   }

   @Override
   public int arrayOffset() {
      return 0;
   }

   @Override
   public boolean hasMemoryAddress() {
      return EMPTY_BYTE_BUFFER_ADDRESS != 0L;
   }

   @Override
   public long memoryAddress() {
      if (this.hasMemoryAddress()) {
         return EMPTY_BYTE_BUFFER_ADDRESS;
      } else {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   public boolean isContiguous() {
      return true;
   }

   @Override
   public String toString(Charset charset) {
      return "";
   }

   @Override
   public String toString(int index, int length, Charset charset) {
      this.checkIndex(index, length);
      return this.toString(charset);
   }

   @Override
   public int hashCode() {
      return 1;
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof ByteBuf && !((ByteBuf)obj).isReadable();
   }

   @Override
   public int compareTo(ByteBuf buffer) {
      return buffer.isReadable() ? -1 : 0;
   }

   @Override
   public String toString() {
      return this.str;
   }

   @Override
   public boolean isReadable(int size) {
      return false;
   }

   @Override
   public boolean isWritable(int size) {
      return false;
   }

   @Override
   public int refCnt() {
      return 1;
   }

   @Override
   public ByteBuf retain() {
      return this;
   }

   @Override
   public ByteBuf retain(int increment) {
      return this;
   }

   @Override
   public ByteBuf touch() {
      return this;
   }

   @Override
   public ByteBuf touch(Object hint) {
      return this;
   }

   @Override
   public boolean release() {
      return false;
   }

   @Override
   public boolean release(int decrement) {
      return false;
   }

   private ByteBuf checkIndex(int index) {
      if (index != 0) {
         throw new IndexOutOfBoundsException();
      } else {
         return this;
      }
   }

   private ByteBuf checkIndex(int index, int length) {
      ObjectUtil.checkPositiveOrZero(length, "length");
      if (index == 0 && length == 0) {
         return this;
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   private ByteBuf checkLength(int length) {
      ObjectUtil.checkPositiveOrZero(length, "length");
      if (length != 0) {
         throw new IndexOutOfBoundsException();
      } else {
         return this;
      }
   }

   static {
      long emptyByteBufferAddress = 0L;

      try {
         if (PlatformDependent.hasUnsafe()) {
            emptyByteBufferAddress = PlatformDependent.directBufferAddress(EMPTY_BYTE_BUFFER);
         }
      } catch (Throwable var3) {
      }

      EMPTY_BYTE_BUFFER_ADDRESS = emptyByteBufferAddress;
   }
}
