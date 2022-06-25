package io.micronaut.core.io;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Named;
import io.micronaut.core.util.ArgumentUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.annotation.concurrent.Immutable;

@Immutable
public interface Readable extends Named {
   @NonNull
   InputStream asInputStream() throws IOException;

   boolean exists();

   default Reader asReader() throws IOException {
      return this.asReader(StandardCharsets.UTF_8);
   }

   default Reader asReader(Charset charset) throws IOException {
      ArgumentUtils.requireNonNull("charset", charset);
      return new InputStreamReader(this.asInputStream(), charset);
   }

   @NonNull
   static Readable of(@NonNull URL url) {
      return new UrlReadable(url);
   }

   @NonNull
   static Readable of(@NonNull File file) {
      ArgumentUtils.requireNonNull("file", file);
      return new FileReadable(file);
   }

   @NonNull
   static Readable of(@NonNull Path path) {
      ArgumentUtils.requireNonNull("path", path);
      return new FileReadable(path.toFile());
   }
}
