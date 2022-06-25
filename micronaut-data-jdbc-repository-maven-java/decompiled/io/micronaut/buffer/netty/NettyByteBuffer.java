package io.micronaut.buffer.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.io.buffer.ReferenceCounted;
import io.micronaut.core.util.ArrayUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

@Internal
class NettyByteBuffer implements ByteBuffer<ByteBuf>, ReferenceCounted {
   private ByteBuf delegate;

   NettyByteBuffer(ByteBuf delegate) {
      this.delegate = delegate;
   }

   @Override
   public ByteBuffer retain() {
      this.delegate.retain();
      return this;
   }

   public ByteBuf asNativeBuffer() {
      return this.delegate;
   }

   @Override
   public boolean release() {
      return this.delegate.release();
   }

   @Override
   public int readableBytes() {
      return this.delegate.readableBytes();
   }

   @Override
   public int writableBytes() {
      return this.delegate.writableBytes();
   }

   @Override
   public int maxCapacity() {
      return this.delegate.maxCapacity();
   }

   @Override
   public ByteBuffer capacity(int capacity) {
      this.delegate.capacity(capacity);
      return this;
   }

   @Override
   public int readerIndex() {
      return this.delegate.readerIndex();
   }

   @Override
   public ByteBuffer readerIndex(int readPosition) {
      this.delegate.readerIndex(readPosition);
      return this;
   }

   @Override
   public int writerIndex() {
      return this.delegate.writerIndex();
   }

   @Override
   public ByteBuffer writerIndex(int position) {
      this.delegate.writerIndex(position);
      return this;
   }

   @Override
   public byte read() {
      return this.delegate.readByte();
   }

   @Override
   public CharSequence readCharSequence(int length, Charset charset) {
      return this.delegate.readCharSequence(length, charset);
   }

   @Override
   public ByteBuffer read(byte[] destination) {
      this.delegate.readBytes(destination);
      return this;
   }

   @Override
   public ByteBuffer read(byte[] destination, int offset, int length) {
      this.delegate.readBytes(destination, offset, length);
      return this;
   }

   @Override
   public ByteBuffer write(byte b) {
      this.delegate.writeByte(b);
      return this;
   }

   @Override
   public ByteBuffer write(byte[] source) {
      this.delegate.writeBytes(source);
      return this;
   }

   @Override
   public ByteBuffer write(CharSequence source, Charset charset) {
      this.delegate.writeCharSequence(source, charset);
      return this;
   }

   @Override
   public ByteBuffer write(byte[] source, int offset, int length) {
      this.delegate.writeBytes(source, offset, length);
      return this;
   }

   @Override
   public ByteBuffer write(ByteBuffer... buffers) {
      if (ArrayUtils.isNotEmpty(buffers)) {
         ByteBuf[] byteBufs = (ByteBuf[])Arrays.stream(buffers)
            .map(buffer -> buffer instanceof NettyByteBuffer ? ((NettyByteBuffer)buffer).asNativeBuffer() : Unpooled.wrappedBuffer(buffer.asNioBuffer()))
            .toArray(x$0 -> new ByteBuf[x$0]);
         return this.write(byteBufs);
      } else {
         return this;
      }
   }

   @Override
   public ByteBuffer write(java.nio.ByteBuffer... buffers) {
      if (ArrayUtils.isNotEmpty(buffers)) {
         ByteBuf[] byteBufs = (ByteBuf[])Arrays.stream(buffers).map(Unpooled::wrappedBuffer).toArray(x$0 -> new ByteBuf[x$0]);
         return this.write(byteBufs);
      } else {
         return this;
      }
   }

   public ByteBuffer write(ByteBuf... byteBufs) {
      if (this.delegate instanceof CompositeByteBuf) {
         CompositeByteBuf compositeByteBuf = (CompositeByteBuf)this.delegate;
         compositeByteBuf.addComponents(true, byteBufs);
      } else {
         ByteBuf current = this.delegate;
         CompositeByteBuf composite = current.alloc().compositeBuffer(byteBufs.length + 1);
         this.delegate = composite;
         composite.addComponent(true, current);
         composite.addComponents(true, byteBufs);
      }

      return this;
   }

   @Override
   public ByteBuffer slice(int index, int length) {
      return new NettyByteBuffer(this.delegate.slice(index, length));
   }

   @Override
   public java.nio.ByteBuffer asNioBuffer() {
      return this.delegate.nioBuffer();
   }

   @Override
   public java.nio.ByteBuffer asNioBuffer(int index, int length) {
      return this.delegate.nioBuffer(index, length);
   }

   @Override
   public InputStream toInputStream() {
      return new ByteBufInputStream(this.delegate);
   }

   @Override
   public OutputStream toOutputStream() {
      return new ByteBufOutputStream(this.delegate);
   }

   @Override
   public byte[] toByteArray() {
      return ByteBufUtil.getBytes(this.delegate);
   }

   @Override
   public String toString(Charset charset) {
      return this.delegate.toString(charset);
   }

   @Override
   public int indexOf(byte b) {
      return this.delegate.bytesBefore(b);
   }

   @Override
   public byte getByte(int index) {
      return this.delegate.getByte(index);
   }
}
