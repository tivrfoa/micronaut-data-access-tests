package io.micronaut.http.multipart;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.MediaType;
import java.util.Optional;

@Internal
public interface FileUpload {
   Optional<MediaType> getContentType();

   String getName();

   String getFilename();

   long getSize();

   long getDefinedSize();

   boolean isComplete();

   default void discard() {
      throw new UnsupportedOperationException("Discard not supported");
   }
}
