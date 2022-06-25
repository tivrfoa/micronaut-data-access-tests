package io.micronaut.core.io;

import io.micronaut.core.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public interface Streamable {
   void writeTo(OutputStream outputStream, @Nullable Charset charset) throws IOException;

   default void writeTo(File file) throws IOException {
      OutputStream outputStream = Files.newOutputStream(file.toPath());
      Throwable var3 = null;

      try {
         this.writeTo(outputStream);
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if (outputStream != null) {
            if (var3 != null) {
               try {
                  outputStream.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               outputStream.close();
            }
         }

      }

   }

   default void writeTo(OutputStream outputStream) throws IOException {
      this.writeTo(outputStream, StandardCharsets.UTF_8);
   }
}
