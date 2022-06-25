package io.micronaut.core.io.buffer;

public interface ByteBufferFactory<T, B> {
   T getNativeAllocator();

   ByteBuffer<B> buffer();

   ByteBuffer<B> buffer(int initialCapacity);

   ByteBuffer<B> buffer(int initialCapacity, int maxCapacity);

   ByteBuffer<B> copiedBuffer(byte[] bytes);

   ByteBuffer<B> copiedBuffer(java.nio.ByteBuffer nioBuffer);

   ByteBuffer<B> wrap(B existing);

   ByteBuffer<B> wrap(byte[] existing);
}
