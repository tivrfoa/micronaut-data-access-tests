package io.micronaut.core.io.buffer;

public interface ReferenceCounted {
   ByteBuffer retain();

   boolean release();
}
