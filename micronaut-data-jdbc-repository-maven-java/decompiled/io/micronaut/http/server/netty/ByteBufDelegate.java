package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

@Internal
public class ByteBufDelegate extends ByteBuf {
   private final ByteBuf byteBuf;

   public ByteBufDelegate(ByteBuf byteBuf) {
      this.byteBuf = byteBuf;
   }

   @Override
   public final boolean hasMemoryAddress() {
      return this.byteBuf.hasMemoryAddress();
   }

   @Override
   public final long memoryAddress() {
      return this.byteBuf.memoryAddress();
   }

   @Override
   public final int capacity() {
      return this.byteBuf.capacity();
   }

   @Override
   public ByteBuf capacity(int newCapacity) {
      this.byteBuf.capacity(newCapacity);
      return this;
   }

   @Override
   public final int maxCapacity() {
      return this.byteBuf.maxCapacity();
   }

   @Override
   public final ByteBufAllocator alloc() {
      return this.byteBuf.alloc();
   }

   @Deprecated
   @Override
   public final ByteOrder order() {
      return this.byteBuf.order();
   }

   @Deprecated
   @Override
   public ByteBuf order(ByteOrder endianness) {
      return this.byteBuf.order(endianness);
   }

   @Override
   public final ByteBuf unwrap() {
      return this.byteBuf;
   }

   @Override
   public ByteBuf asReadOnly() {
      return this.byteBuf.asReadOnly();
   }

   @Override
   public boolean isReadOnly() {
      return this.byteBuf.isReadOnly();
   }

   @Override
   public final boolean isDirect() {
      return this.byteBuf.isDirect();
   }

   @Override
   public final int readerIndex() {
      return this.byteBuf.readerIndex();
   }

   @Override
   public final ByteBuf readerIndex(int readerIndex) {
      this.byteBuf.readerIndex(readerIndex);
      return this;
   }

   @Override
   public final int writerIndex() {
      return this.byteBuf.writerIndex();
   }

   @Override
   public final ByteBuf writerIndex(int writerIndex) {
      this.byteBuf.writerIndex(writerIndex);
      return this;
   }

   @Override
   public ByteBuf setIndex(int readerIndex, int writerIndex) {
      this.byteBuf.setIndex(readerIndex, writerIndex);
      return this;
   }

   @Override
   public final int readableBytes() {
      return this.byteBuf.readableBytes();
   }

   @Override
   public final int writableBytes() {
      return this.byteBuf.writableBytes();
   }

   @Override
   public final int maxWritableBytes() {
      return this.byteBuf.maxWritableBytes();
   }

   @Override
   public final boolean isReadable() {
      return this.byteBuf.isReadable();
   }

   @Override
   public final boolean isWritable() {
      return this.byteBuf.isWritable();
   }

   @Override
   public final ByteBuf clear() {
      this.byteBuf.clear();
      return this;
   }

   @Override
   public final ByteBuf markReaderIndex() {
      this.byteBuf.markReaderIndex();
      return this;
   }

   @Override
   public final ByteBuf resetReaderIndex() {
      this.byteBuf.resetReaderIndex();
      return this;
   }

   @Override
   public final ByteBuf markWriterIndex() {
      this.byteBuf.markWriterIndex();
      return this;
   }

   @Override
   public final ByteBuf resetWriterIndex() {
      this.byteBuf.resetWriterIndex();
      return this;
   }

   @Override
   public ByteBuf discardReadBytes() {
      this.byteBuf.discardReadBytes();
      return this;
   }

   @Override
   public ByteBuf discardSomeReadBytes() {
      this.byteBuf.discardSomeReadBytes();
      return this;
   }

   @Override
   public ByteBuf ensureWritable(int minWritableBytes) {
      this.byteBuf.ensureWritable(minWritableBytes);
      return this;
   }

   @Override
   public int ensureWritable(int minWritableBytes, boolean force) {
      return this.byteBuf.ensureWritable(minWritableBytes, force);
   }

   @Override
   public boolean getBoolean(int index) {
      return this.byteBuf.getBoolean(index);
   }

   @Override
   public byte getByte(int index) {
      return this.byteBuf.getByte(index);
   }

   @Override
   public short getUnsignedByte(int index) {
      return this.byteBuf.getUnsignedByte(index);
   }

   @Override
   public short getShort(int index) {
      return this.byteBuf.getShort(index);
   }

   @Override
   public short getShortLE(int index) {
      return this.byteBuf.getShortLE(index);
   }

   @Override
   public int getUnsignedShort(int index) {
      return this.byteBuf.getUnsignedShort(index);
   }

   @Override
   public int getUnsignedShortLE(int index) {
      return this.byteBuf.getUnsignedShortLE(index);
   }

   @Override
   public int getMedium(int index) {
      return this.byteBuf.getMedium(index);
   }

   @Override
   public int getMediumLE(int index) {
      return this.byteBuf.getMediumLE(index);
   }

   @Override
   public int getUnsignedMedium(int index) {
      return this.byteBuf.getUnsignedMedium(index);
   }

   @Override
   public int getUnsignedMediumLE(int index) {
      return this.byteBuf.getUnsignedMediumLE(index);
   }

   @Override
   public int getInt(int index) {
      return this.byteBuf.getInt(index);
   }

   @Override
   public int getIntLE(int index) {
      return this.byteBuf.getIntLE(index);
   }

   @Override
   public long getUnsignedInt(int index) {
      return this.byteBuf.getUnsignedInt(index);
   }

   @Override
   public long getUnsignedIntLE(int index) {
      return this.byteBuf.getUnsignedIntLE(index);
   }

   @Override
   public long getLong(int index) {
      return this.byteBuf.getLong(index);
   }

   @Override
   public long getLongLE(int index) {
      return this.byteBuf.getLongLE(index);
   }

   @Override
   public char getChar(int index) {
      return this.byteBuf.getChar(index);
   }

   @Override
   public float getFloat(int index) {
      return this.byteBuf.getFloat(index);
   }

   @Override
   public double getDouble(int index) {
      return this.byteBuf.getDouble(index);
   }

   @Override
   public ByteBuf getBytes(int index, ByteBuf dst) {
      this.byteBuf.getBytes(index, dst);
      return this;
   }

   @Override
   public ByteBuf getBytes(int index, ByteBuf dst, int length) {
      this.byteBuf.getBytes(index, dst, length);
      return this;
   }

   @Override
   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
      this.byteBuf.getBytes(index, dst, dstIndex, length);
      return this;
   }

   @Override
   public ByteBuf getBytes(int index, byte[] dst) {
      this.byteBuf.getBytes(index, dst);
      return this;
   }

   @Override
   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
      this.byteBuf.getBytes(index, dst, dstIndex, length);
      return this;
   }

   @Override
   public ByteBuf getBytes(int index, ByteBuffer dst) {
      this.byteBuf.getBytes(index, dst);
      return this;
   }

   @Override
   public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
      this.byteBuf.getBytes(index, out, length);
      return this;
   }

   @Override
   public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
      return this.byteBuf.getBytes(index, out, length);
   }

   @Override
   public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
      return this.byteBuf.getBytes(index, out, position, length);
   }

   @Override
   public CharSequence getCharSequence(int index, int length, Charset charset) {
      return this.byteBuf.getCharSequence(index, length, charset);
   }

   @Override
   public ByteBuf setBoolean(int index, boolean value) {
      this.byteBuf.setBoolean(index, value);
      return this;
   }

   @Override
   public ByteBuf setByte(int index, int value) {
      this.byteBuf.setByte(index, value);
      return this;
   }

   @Override
   public ByteBuf setShort(int index, int value) {
      this.byteBuf.setShort(index, value);
      return this;
   }

   @Override
   public ByteBuf setShortLE(int index, int value) {
      this.byteBuf.setShortLE(index, value);
      return this;
   }

   @Override
   public ByteBuf setMedium(int index, int value) {
      this.byteBuf.setMedium(index, value);
      return this;
   }

   @Override
   public ByteBuf setMediumLE(int index, int value) {
      this.byteBuf.setMediumLE(index, value);
      return this;
   }

   @Override
   public ByteBuf setInt(int index, int value) {
      this.byteBuf.setInt(index, value);
      return this;
   }

   @Override
   public ByteBuf setIntLE(int index, int value) {
      this.byteBuf.setIntLE(index, value);
      return this;
   }

   @Override
   public ByteBuf setLong(int index, long value) {
      this.byteBuf.setLong(index, value);
      return this;
   }

   @Override
   public ByteBuf setLongLE(int index, long value) {
      this.byteBuf.setLongLE(index, value);
      return this;
   }

   @Override
   public ByteBuf setChar(int index, int value) {
      this.byteBuf.setChar(index, value);
      return this;
   }

   @Override
   public ByteBuf setFloat(int index, float value) {
      this.byteBuf.setFloat(index, value);
      return this;
   }

   @Override
   public ByteBuf setDouble(int index, double value) {
      this.byteBuf.setDouble(index, value);
      return this;
   }

   @Override
   public ByteBuf setBytes(int index, ByteBuf src) {
      this.byteBuf.setBytes(index, src);
      return this;
   }

   @Override
   public ByteBuf setBytes(int index, ByteBuf src, int length) {
      this.byteBuf.setBytes(index, src, length);
      return this;
   }

   @Override
   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
      this.byteBuf.setBytes(index, src, srcIndex, length);
      return this;
   }

   @Override
   public ByteBuf setBytes(int index, byte[] src) {
      this.byteBuf.setBytes(index, src);
      return this;
   }

   @Override
   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
      this.byteBuf.setBytes(index, src, srcIndex, length);
      return this;
   }

   @Override
   public ByteBuf setBytes(int index, ByteBuffer src) {
      this.byteBuf.setBytes(index, src);
      return this;
   }

   @Override
   public int setBytes(int index, InputStream in, int length) throws IOException {
      return this.byteBuf.setBytes(index, in, length);
   }

   @Override
   public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
      return this.byteBuf.setBytes(index, in, length);
   }

   @Override
   public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
      return this.byteBuf.setBytes(index, in, position, length);
   }

   @Override
   public ByteBuf setZero(int index, int length) {
      this.byteBuf.setZero(index, length);
      return this;
   }

   @Override
   public int setCharSequence(int index, CharSequence sequence, Charset charset) {
      return this.byteBuf.setCharSequence(index, sequence, charset);
   }

   @Override
   public boolean readBoolean() {
      return this.byteBuf.readBoolean();
   }

   @Override
   public byte readByte() {
      return this.byteBuf.readByte();
   }

   @Override
   public short readUnsignedByte() {
      return this.byteBuf.readUnsignedByte();
   }

   @Override
   public short readShort() {
      return this.byteBuf.readShort();
   }

   @Override
   public short readShortLE() {
      return this.byteBuf.readShortLE();
   }

   @Override
   public int readUnsignedShort() {
      return this.byteBuf.readUnsignedShort();
   }

   @Override
   public int readUnsignedShortLE() {
      return this.byteBuf.readUnsignedShortLE();
   }

   @Override
   public int readMedium() {
      return this.byteBuf.readMedium();
   }

   @Override
   public int readMediumLE() {
      return this.byteBuf.readMediumLE();
   }

   @Override
   public int readUnsignedMedium() {
      return this.byteBuf.readUnsignedMedium();
   }

   @Override
   public int readUnsignedMediumLE() {
      return this.byteBuf.readUnsignedMediumLE();
   }

   @Override
   public int readInt() {
      return this.byteBuf.readInt();
   }

   @Override
   public int readIntLE() {
      return this.byteBuf.readIntLE();
   }

   @Override
   public long readUnsignedInt() {
      return this.byteBuf.readUnsignedInt();
   }

   @Override
   public long readUnsignedIntLE() {
      return this.byteBuf.readUnsignedIntLE();
   }

   @Override
   public long readLong() {
      return this.byteBuf.readLong();
   }

   @Override
   public long readLongLE() {
      return this.byteBuf.readLongLE();
   }

   @Override
   public char readChar() {
      return this.byteBuf.readChar();
   }

   @Override
   public float readFloat() {
      return this.byteBuf.readFloat();
   }

   @Override
   public double readDouble() {
      return this.byteBuf.readDouble();
   }

   @Override
   public ByteBuf readBytes(int length) {
      return this.byteBuf.readBytes(length);
   }

   @Override
   public ByteBuf readSlice(int length) {
      return this.byteBuf.readSlice(length);
   }

   @Override
   public ByteBuf readRetainedSlice(int length) {
      return this.byteBuf.readRetainedSlice(length);
   }

   @Override
   public ByteBuf readBytes(ByteBuf dst) {
      this.byteBuf.readBytes(dst);
      return this;
   }

   @Override
   public ByteBuf readBytes(ByteBuf dst, int length) {
      this.byteBuf.readBytes(dst, length);
      return this;
   }

   @Override
   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
      this.byteBuf.readBytes(dst, dstIndex, length);
      return this;
   }

   @Override
   public ByteBuf readBytes(byte[] dst) {
      this.byteBuf.readBytes(dst);
      return this;
   }

   @Override
   public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
      this.byteBuf.readBytes(dst, dstIndex, length);
      return this;
   }

   @Override
   public ByteBuf readBytes(ByteBuffer dst) {
      this.byteBuf.readBytes(dst);
      return this;
   }

   @Override
   public ByteBuf readBytes(OutputStream out, int length) throws IOException {
      this.byteBuf.readBytes(out, length);
      return this;
   }

   @Override
   public int readBytes(GatheringByteChannel out, int length) throws IOException {
      return this.byteBuf.readBytes(out, length);
   }

   @Override
   public int readBytes(FileChannel out, long position, int length) throws IOException {
      return this.byteBuf.readBytes(out, position, length);
   }

   @Override
   public CharSequence readCharSequence(int length, Charset charset) {
      return this.byteBuf.readCharSequence(length, charset);
   }

   @Override
   public ByteBuf skipBytes(int length) {
      this.byteBuf.skipBytes(length);
      return this;
   }

   @Override
   public ByteBuf writeBoolean(boolean value) {
      this.byteBuf.writeBoolean(value);
      return this;
   }

   @Override
   public ByteBuf writeByte(int value) {
      this.byteBuf.writeByte(value);
      return this;
   }

   @Override
   public ByteBuf writeShort(int value) {
      this.byteBuf.writeShort(value);
      return this;
   }

   @Override
   public ByteBuf writeShortLE(int value) {
      this.byteBuf.writeShortLE(value);
      return this;
   }

   @Override
   public ByteBuf writeMedium(int value) {
      this.byteBuf.writeMedium(value);
      return this;
   }

   @Override
   public ByteBuf writeMediumLE(int value) {
      this.byteBuf.writeMediumLE(value);
      return this;
   }

   @Override
   public ByteBuf writeInt(int value) {
      this.byteBuf.writeInt(value);
      return this;
   }

   @Override
   public ByteBuf writeIntLE(int value) {
      this.byteBuf.writeIntLE(value);
      return this;
   }

   @Override
   public ByteBuf writeLong(long value) {
      this.byteBuf.writeLong(value);
      return this;
   }

   @Override
   public ByteBuf writeLongLE(long value) {
      this.byteBuf.writeLongLE(value);
      return this;
   }

   @Override
   public ByteBuf writeChar(int value) {
      this.byteBuf.writeChar(value);
      return this;
   }

   @Override
   public ByteBuf writeFloat(float value) {
      this.byteBuf.writeFloat(value);
      return this;
   }

   @Override
   public ByteBuf writeDouble(double value) {
      this.byteBuf.writeDouble(value);
      return this;
   }

   @Override
   public ByteBuf writeBytes(ByteBuf src) {
      this.byteBuf.writeBytes(src);
      return this;
   }

   @Override
   public ByteBuf writeBytes(ByteBuf src, int length) {
      this.byteBuf.writeBytes(src, length);
      return this;
   }

   @Override
   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
      this.byteBuf.writeBytes(src, srcIndex, length);
      return this;
   }

   @Override
   public ByteBuf writeBytes(byte[] src) {
      this.byteBuf.writeBytes(src);
      return this;
   }

   @Override
   public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
      this.byteBuf.writeBytes(src, srcIndex, length);
      return this;
   }

   @Override
   public ByteBuf writeBytes(ByteBuffer src) {
      this.byteBuf.writeBytes(src);
      return this;
   }

   @Override
   public int writeBytes(InputStream in, int length) throws IOException {
      return this.byteBuf.writeBytes(in, length);
   }

   @Override
   public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
      return this.byteBuf.writeBytes(in, length);
   }

   @Override
   public int writeBytes(FileChannel in, long position, int length) throws IOException {
      return this.byteBuf.writeBytes(in, position, length);
   }

   @Override
   public ByteBuf writeZero(int length) {
      this.byteBuf.writeZero(length);
      return this;
   }

   @Override
   public int writeCharSequence(CharSequence sequence, Charset charset) {
      return this.byteBuf.writeCharSequence(sequence, charset);
   }

   @Override
   public int indexOf(int fromIndex, int toIndex, byte value) {
      return this.byteBuf.indexOf(fromIndex, toIndex, value);
   }

   @Override
   public int bytesBefore(byte value) {
      return this.byteBuf.bytesBefore(value);
   }

   @Override
   public int bytesBefore(int length, byte value) {
      return this.byteBuf.bytesBefore(length, value);
   }

   @Override
   public int bytesBefore(int index, int length, byte value) {
      return this.byteBuf.bytesBefore(index, length, value);
   }

   @Override
   public int forEachByte(ByteProcessor processor) {
      return this.byteBuf.forEachByte(processor);
   }

   @Override
   public int forEachByte(int index, int length, ByteProcessor processor) {
      return this.byteBuf.forEachByte(index, length, processor);
   }

   @Override
   public int forEachByteDesc(ByteProcessor processor) {
      return this.byteBuf.forEachByteDesc(processor);
   }

   @Override
   public int forEachByteDesc(int index, int length, ByteProcessor processor) {
      return this.byteBuf.forEachByteDesc(index, length, processor);
   }

   @Override
   public ByteBuf copy() {
      return this.byteBuf.copy();
   }

   @Override
   public ByteBuf copy(int index, int length) {
      return this.byteBuf.copy(index, length);
   }

   @Override
   public ByteBuf slice() {
      return this.byteBuf.slice();
   }

   @Override
   public ByteBuf retainedSlice() {
      return this.byteBuf.retainedSlice();
   }

   @Override
   public ByteBuf slice(int index, int length) {
      return this.byteBuf.slice(index, length);
   }

   @Override
   public ByteBuf retainedSlice(int index, int length) {
      return this.byteBuf.retainedSlice(index, length);
   }

   @Override
   public ByteBuf duplicate() {
      return this.byteBuf.duplicate();
   }

   @Override
   public ByteBuf retainedDuplicate() {
      return this.byteBuf.retainedDuplicate();
   }

   @Override
   public int nioBufferCount() {
      return this.byteBuf.nioBufferCount();
   }

   @Override
   public ByteBuffer nioBuffer() {
      return this.byteBuf.nioBuffer();
   }

   @Override
   public ByteBuffer nioBuffer(int index, int length) {
      return this.byteBuf.nioBuffer(index, length);
   }

   @Override
   public ByteBuffer[] nioBuffers() {
      return this.byteBuf.nioBuffers();
   }

   @Override
   public ByteBuffer[] nioBuffers(int index, int length) {
      return this.byteBuf.nioBuffers(index, length);
   }

   @Override
   public ByteBuffer internalNioBuffer(int index, int length) {
      return this.byteBuf.internalNioBuffer(index, length);
   }

   @Override
   public boolean hasArray() {
      return this.byteBuf.hasArray();
   }

   @Override
   public byte[] array() {
      return this.byteBuf.array();
   }

   @Override
   public int arrayOffset() {
      return this.byteBuf.arrayOffset();
   }

   @Override
   public String toString(Charset charset) {
      return this.byteBuf.toString(charset);
   }

   @Override
   public String toString(int index, int length, Charset charset) {
      return this.byteBuf.toString(index, length, charset);
   }

   @Override
   public int hashCode() {
      return this.byteBuf.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return this.byteBuf.equals(obj);
   }

   @Override
   public int compareTo(ByteBuf buffer) {
      return this.byteBuf.compareTo(buffer);
   }

   @Override
   public String toString() {
      return StringUtil.simpleClassName(this) + '(' + this.byteBuf.toString() + ')';
   }

   @Override
   public ByteBuf retain(int increment) {
      this.byteBuf.retain(increment);
      return this;
   }

   @Override
   public ByteBuf retain() {
      this.byteBuf.retain();
      return this;
   }

   @Override
   public ByteBuf touch() {
      this.byteBuf.touch();
      return this;
   }

   @Override
   public ByteBuf touch(Object hint) {
      this.byteBuf.touch(hint);
      return this;
   }

   @Override
   public final boolean isReadable(int size) {
      return this.byteBuf.isReadable(size);
   }

   @Override
   public final boolean isWritable(int size) {
      return this.byteBuf.isWritable(size);
   }

   @Override
   public final int refCnt() {
      return this.byteBuf.refCnt();
   }

   @Override
   public boolean release() {
      return this.byteBuf.release();
   }

   @Override
   public boolean release(int decrement) {
      return this.byteBuf.release(decrement);
   }
}
