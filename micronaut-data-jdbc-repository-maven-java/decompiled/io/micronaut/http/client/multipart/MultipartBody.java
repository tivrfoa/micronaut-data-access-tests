package io.micronaut.http.client.multipart;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.MediaType;
import io.micronaut.http.multipart.MultipartException;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class MultipartBody {
   private final List<Part<?>> parts;

   private MultipartBody(List<Part<?>> parts) {
      this.parts = parts;
   }

   @Internal
   public <T> List<T> getData(MultipartDataFactory<T> factory) {
      List<T> data = new ArrayList(this.parts.size());

      for(Part<?> part : this.parts) {
         data.add(part.<T>getData(factory));
      }

      return data;
   }

   public static MultipartBody.Builder builder() {
      return new MultipartBody.Builder();
   }

   public static final class Builder {
      private List<Part<?>> parts = new ArrayList();

      private Builder() {
      }

      public MultipartBody.Builder addPart(String name, File file) {
         return this.addPart(name, file.getName(), file);
      }

      public MultipartBody.Builder addPart(String name, String filename, File file) {
         return this.addFilePart(new FilePart(name, filename, file));
      }

      public MultipartBody.Builder addPart(String name, String filename, MediaType contentType, File file) {
         return this.addFilePart(new FilePart(name, filename, contentType, file));
      }

      public MultipartBody.Builder addPart(String name, String filename, byte[] data) {
         return this.addFilePart(new BytePart(name, filename, data));
      }

      public MultipartBody.Builder addPart(String name, String filename, MediaType contentType, byte[] data) {
         return this.addFilePart(new BytePart(name, filename, contentType, data));
      }

      public MultipartBody.Builder addPart(String name, String filename, InputStream data, long contentLength) {
         return this.addFilePart(new InputStreamPart(name, filename, data, contentLength));
      }

      public MultipartBody.Builder addPart(String name, String filename, MediaType contentType, InputStream data, long contentLength) {
         return this.addFilePart(new InputStreamPart(name, filename, contentType, data, contentLength));
      }

      public MultipartBody.Builder addPart(String name, String value) {
         this.parts.add(new StringPart(name, value));
         return this;
      }

      private MultipartBody.Builder addFilePart(AbstractFilePart<?> filePart) {
         this.parts.add(filePart);
         return this;
      }

      public MultipartBody build() throws MultipartException {
         if (this.parts.isEmpty()) {
            throw new MultipartException("Cannot create a MultipartBody with no parts");
         } else {
            return new MultipartBody(this.parts);
         }
      }
   }
}
