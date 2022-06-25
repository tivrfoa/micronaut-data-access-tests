package io.micronaut.buffer.netty;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.io.buffer.ByteBufferFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import jakarta.inject.Singleton;

@Internal
@Singleton
@BootstrapContextCompatible
public class NettyByteBufferFactory implements ByteBufferFactory<ByteBufAllocator, ByteBuf> {
   public static final NettyByteBufferFactory DEFAULT = new NettyByteBufferFactory();
   private final ByteBufAllocator allocator;

   public NettyByteBufferFactory() {
      this.allocator = ByteBufAllocator.DEFAULT;
   }

   public NettyByteBufferFactory(ByteBufAllocator allocator) {
      this.allocator = allocator;
   }

   public ByteBufAllocator getNativeAllocator() {
      return this.allocator;
   }

   @Override
   public ByteBuffer<ByteBuf> buffer() {
      return new NettyByteBuffer(this.allocator.buffer());
   }

   @Override
   public ByteBuffer<ByteBuf> buffer(int initialCapacity) {
      return new NettyByteBuffer(this.allocator.buffer(initialCapacity));
   }

   @Override
   public ByteBuffer<ByteBuf> buffer(int initialCapacity, int maxCapacity) {
      return new NettyByteBuffer(this.allocator.buffer(initialCapacity, maxCapacity));
   }

   @Override
   public ByteBuffer<ByteBuf> copiedBuffer(byte[] bytes) {
      return bytes.length == 0 ? new NettyByteBuffer(Unpooled.EMPTY_BUFFER) : new NettyByteBuffer(Unpooled.copiedBuffer(bytes));
   }

   @Override
   public ByteBuffer<ByteBuf> copiedBuffer(java.nio.ByteBuffer nioBuffer) {
      return new NettyByteBuffer(Unpooled.copiedBuffer(nioBuffer));
   }

   public ByteBuffer<ByteBuf> wrap(ByteBuf existing) {
      return new NettyByteBuffer(existing);
   }

   @Override
   public ByteBuffer<ByteBuf> wrap(byte[] existing) {
      return new NettyByteBuffer(Unpooled.wrappedBuffer(existing));
   }

   static {
      ConversionService.SHARED.addConverter(ByteBuf.class, ByteBuffer.class, DEFAULT::wrap);
      ConversionService.SHARED.addConverter(ByteBuffer.class, ByteBuf.class, byteBuffer -> {
         if (byteBuffer instanceof NettyByteBuffer) {
            return (ByteBuf)byteBuffer.asNativeBuffer();
         } else {
            throw new IllegalArgumentException("Unconvertible buffer type " + byteBuffer);
         }
      });
   }
}
