package io.micronaut.http.client.multipart;

import io.micronaut.http.MediaType;
import java.io.InputStream;

class InputStreamPart extends AbstractFilePart<InputStream> {
   private final InputStream data;
   private final long contentLength;

   InputStreamPart(String name, String filename, InputStream data, long contentLength) {
      this(name, filename, null, data, contentLength);
   }

   InputStreamPart(String name, String filename, MediaType contentType, InputStream data, long contentLength) {
      super(name, filename, contentType);
      this.data = data;
      this.contentLength = contentLength;
   }

   @Override
   long getLength() {
      return this.contentLength;
   }

   InputStream getContent() {
      return this.data;
   }
}
