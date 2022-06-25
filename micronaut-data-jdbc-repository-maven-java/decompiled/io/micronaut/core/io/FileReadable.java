package io.micronaut.core.io;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;

@Internal
class FileReadable implements Readable {
   private final File file;

   FileReadable(@NonNull File file) {
      ArgumentUtils.requireNonNull("file", file);
      this.file = file;
   }

   @NonNull
   @Override
   public InputStream asInputStream() throws IOException {
      return Files.newInputStream(this.file.toPath());
   }

   @Override
   public Reader asReader() throws IOException {
      return Files.newBufferedReader(this.file.toPath());
   }

   @Override
   public Reader asReader(Charset charset) throws IOException {
      return Files.newBufferedReader(this.file.toPath(), charset);
   }

   @Override
   public boolean exists() {
      return this.file.exists();
   }

   @Override
   public String getName() {
      return this.file.getName();
   }
}
