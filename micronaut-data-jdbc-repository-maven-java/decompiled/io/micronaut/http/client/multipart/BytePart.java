package io.micronaut.http.client.multipart;

import io.micronaut.http.MediaType;

class BytePart extends AbstractFilePart<byte[]> {
   private final byte[] data;

   BytePart(String name, String filename, byte[] data) {
      this(name, filename, null, data);
   }

   BytePart(String name, String filename, MediaType contentType, byte[] data) {
      super(name, filename, contentType);
      this.data = data;
   }

   @Override
   long getLength() {
      return (long)this.data.length;
   }

   byte[] getContent() {
      return this.data;
   }
}
