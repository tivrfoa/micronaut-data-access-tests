package io.micronaut.jackson.core.env;

import com.fasterxml.jackson.core.JsonParseException;
import io.micronaut.context.env.CachedEnvironment;
import io.micronaut.context.env.MapPropertySource;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.ResourceLoader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Internal
public class CloudFoundryVcapApplicationPropertySourceLoader extends EnvJsonPropertySourceLoader {
   public static final int POSITION = -40;
   private static final String VCAP_APPLICATION = "VCAP_APPLICATION";

   @Override
   public int getOrder() {
      return -40;
   }

   @Override
   public Set<String> getExtensions() {
      return Collections.singleton("VCAP_APPLICATION");
   }

   @Override
   protected String getEnvValue() {
      return CachedEnvironment.getenv("VCAP_APPLICATION");
   }

   @Override
   protected Optional<InputStream> readInput(ResourceLoader resourceLoader, String fileName) {
      return fileName.equals("application.VCAP_APPLICATION") ? this.getEnvValueAsStream() : Optional.empty();
   }

   @Override
   protected void processInput(String name, InputStream input, Map<String, Object> finalMap) throws IOException {
      try {
         Map<String, Object> map = this.readJsonAsMap(input);
         this.processMap(finalMap, map, "vcap.application.");
      } catch (JsonParseException var5) {
         throw new ConfigurationException("Could not parse 'VCAP_APPLICATION'." + var5.getMessage(), var5);
      }
   }

   @Override
   protected MapPropertySource createPropertySource(String name, Map<String, Object> map, int order) {
      return super.createPropertySource("cloudfoundry-vcap-application", map, order);
   }
}
