package io.micronaut.jackson.core.env;

import io.micronaut.context.env.CachedEnvironment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.ResourceLoader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Internal
public class EnvJsonPropertySourceLoader extends JsonPropertySourceLoader {
   public static final int POSITION = -50;
   private static final String SPRING_APPLICATION_JSON = "SPRING_APPLICATION_JSON";
   private static final String MICRONAUT_APPLICATION_JSON = "MICRONAUT_APPLICATION_JSON";

   @Override
   public int getOrder() {
      return -50;
   }

   @Override
   protected Optional<InputStream> readInput(ResourceLoader resourceLoader, String fileName) {
      return fileName.equals("application.json") ? this.getEnvValueAsStream() : Optional.empty();
   }

   protected Optional<InputStream> getEnvValueAsStream() {
      String v = this.getEnvValue();
      if (v != null) {
         String encoding = CachedEnvironment.getProperty("file.encoding");
         Charset charset = encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8;
         return Optional.of(new ByteArrayInputStream(v.getBytes(charset)));
      } else {
         return Optional.empty();
      }
   }

   protected String getEnvValue() {
      String v = CachedEnvironment.getenv("SPRING_APPLICATION_JSON");
      if (v == null) {
         v = CachedEnvironment.getenv("MICRONAUT_APPLICATION_JSON");
      }

      return v;
   }
}
