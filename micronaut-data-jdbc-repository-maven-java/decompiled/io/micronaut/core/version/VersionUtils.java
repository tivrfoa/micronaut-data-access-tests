package io.micronaut.core.version;

import io.micronaut.core.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class VersionUtils {
   private static final Properties VERSIONS = new Properties();
   public static final String MICRONAUT_VERSION;

   public static boolean isAtLeastMicronautVersion(String requiredVersion) {
      return MICRONAUT_VERSION == null || SemanticVersion.isAtLeast(MICRONAUT_VERSION, requiredVersion);
   }

   @Nullable
   public static String getMicronautVersion() {
      Object micronautVersion = VERSIONS.get("micronaut.version");
      return micronautVersion != null ? micronautVersion.toString() : null;
   }

   static {
      URL resource = VersionUtils.class.getResource("/micronaut-version.properties");
      if (resource != null) {
         try {
            Reader reader = new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8);
            Throwable var2 = null;

            try {
               VERSIONS.load(reader);
            } catch (Throwable var12) {
               var2 = var12;
               throw var12;
            } finally {
               if (reader != null) {
                  if (var2 != null) {
                     try {
                        reader.close();
                     } catch (Throwable var11) {
                        var2.addSuppressed(var11);
                     }
                  } else {
                     reader.close();
                  }
               }

            }
         } catch (IOException var14) {
         }
      }

      MICRONAUT_VERSION = getMicronautVersion();
   }
}
