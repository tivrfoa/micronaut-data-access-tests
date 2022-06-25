package io.micronaut.http.client.multipart;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.http.MediaType;
import java.io.IOException;
import java.nio.charset.Charset;

abstract class AbstractFilePart<D> extends Part<D> {
   protected final String filename;
   protected final MediaType contentType;

   AbstractFilePart(String name, String filename, @Nullable MediaType contentType) {
      super(name);
      if (filename == null) {
         throw new IllegalArgumentException("Adding file parts with a null filename is not allowed");
      } else {
         this.filename = filename;
         if (contentType == null) {
            this.contentType = (MediaType)MediaType.forExtension(NameUtils.extension(filename)).orElse(MediaType.APPLICATION_OCTET_STREAM_TYPE);
         } else {
            this.contentType = contentType;
         }

      }
   }

   abstract long getLength();

   @NonNull
   @Override
   <T> T getData(MultipartDataFactory<T> factory) {
      MediaType mediaType = this.contentType;
      String encoding = mediaType.isTextBased() ? null : "binary";
      Charset charset = (Charset)mediaType.getCharset().orElse(null);
      T fileUpload = factory.createFileUpload(this.name, this.filename, mediaType, encoding, charset, this.getLength());

      try {
         factory.setContent(fileUpload, this.getContent());
         return fileUpload;
      } catch (IOException var7) {
         throw new IllegalArgumentException(var7);
      }
   }
}
