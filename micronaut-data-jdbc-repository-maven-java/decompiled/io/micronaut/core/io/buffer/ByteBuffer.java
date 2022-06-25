package io.micronaut.core.io.buffer;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public interface ByteBuffer<T> {
   T asNativeBuffer();

   int readableBytes();

   int writableBytes();

   int maxCapacity();

   ByteBuffer capacity(int capacity);

   int readerIndex();

   ByteBuffer readerIndex(int readPosition);

   int writerIndex();

   ByteBuffer writerIndex(int position);

   byte read();

   CharSequence readCharSequence(int length, Charset charset);

   ByteBuffer read(byte[] destination);

   ByteBuffer read(byte[] destination, int offset, int length);

   ByteBuffer write(byte b);

   ByteBuffer write(byte[] source);

   ByteBuffer write(CharSequence source, Charset charset);

   ByteBuffer write(byte[] source, int offset, int length);

   ByteBuffer write(ByteBuffer... buffers);

   ByteBuffer write(java.nio.ByteBuffer... buffers);

   ByteBuffer slice(int index, int length);

   java.nio.ByteBuffer asNioBuffer();

   java.nio.ByteBuffer asNioBuffer(int index, int length);

   InputStream toInputStream();

   OutputStream toOutputStream();

   byte[] toByteArray();

   String toString(Charset charset);

   int indexOf(byte b);

   byte getByte(int index);
}
