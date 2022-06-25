package io.micronaut.http.client.multipart;

import io.micronaut.http.MediaType;
import java.io.File;

class FilePart extends AbstractFilePart<File> {
   private final File data;

   FilePart(String name, String filename, File data) {
      this(name, filename, null, data);
   }

   FilePart(String name, String filename, MediaType contentType, File data) {
      super(name, filename, contentType);
      this.data = data;
   }

   @Override
   long getLength() {
      return this.data.length();
   }

   File getContent() {
      return this.data;
   }
}
