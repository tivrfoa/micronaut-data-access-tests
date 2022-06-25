package io.micronaut.http.client.multipart;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import java.io.IOException;
import java.nio.charset.Charset;

public interface MultipartDataFactory<T> {
   @NonNull
   T createFileUpload(
      @NonNull String name, @NonNull String filename, @NonNull MediaType contentType, @Nullable String encoding, @Nullable Charset charset, long length
   );

   @NonNull
   T createAttribute(@NonNull String name, @NonNull String value);

   void setContent(T fileUploadObject, Object content) throws IOException;
}
