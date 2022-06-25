package org.flywaydb.core.internal.util;

public class IOUtils {
   public static void close(AutoCloseable closeable) {
      if (closeable != null) {
         try {
            closeable.close();
         } catch (Exception var2) {
         }

      }
   }

   private IOUtils() {
   }
}
