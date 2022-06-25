package io.micronaut.context.env;

import io.micronaut.context.exceptions.ConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface PropertySourceReader {
   Map<String, Object> read(String name, InputStream input) throws IOException;

   default Set<String> getExtensions() {
      return Collections.emptySet();
   }

   default Map<String, Object> read(String name, byte[] bytes) {
      try {
         InputStream input = new ByteArrayInputStream(bytes);
         Throwable var4 = null;

         Map var5;
         try {
            var5 = this.read(name, input);
         } catch (Throwable var15) {
            var4 = var15;
            throw var15;
         } finally {
            if (input != null) {
               if (var4 != null) {
                  try {
                     input.close();
                  } catch (Throwable var14) {
                     var4.addSuppressed(var14);
                  }
               } else {
                  input.close();
               }
            }

         }

         return var5;
      } catch (Throwable var17) {
         throw new ConfigurationException("Error reading property source [" + name + "]: " + var17.getMessage(), var17);
      }
   }
}
