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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Internal
public class CloudFoundryVcapServicesPropertySourceLoader extends EnvJsonPropertySourceLoader {
   public static final int POSITION = -39;
   private static final String VCAP_SERVICES = "VCAP_SERVICES";

   @Override
   public int getOrder() {
      return -39;
   }

   @Override
   protected String getEnvValue() {
      return CachedEnvironment.getenv("VCAP_SERVICES");
   }

   @Override
   public Set<String> getExtensions() {
      return Collections.singleton("VCAP_SERVICES");
   }

   @Override
   protected Optional<InputStream> readInput(ResourceLoader resourceLoader, String fileName) {
      return fileName.equals("application.VCAP_SERVICES") ? this.getEnvValueAsStream() : Optional.empty();
   }

   @Override
   protected void processInput(String name, InputStream input, Map<String, Object> finalMap) throws IOException {
      try {
         Map<String, Object> map = this.readJsonAsMap(input);
         this.processVcapServices(finalMap, map);
      } catch (JsonParseException var5) {
         throw new ConfigurationException("Could not parse 'VCAP_SERVICES': " + var5.getMessage(), var5);
      }
   }

   private void processVcapServices(Map<String, Object> finalMap, Map<String, Object> vcapServices) {
      if (vcapServices != null) {
         for(Object services : vcapServices.values()) {
            for(Object object : (List)services) {
               Map<String, Object> service = (Map)object;
               String key = (String)service.get("name");
               if (key == null) {
                  key = (String)service.get("label");
               }

               this.processMap(finalMap, service, "vcap.services." + key + ".");
            }
         }
      }

   }

   @Override
   protected MapPropertySource createPropertySource(String name, Map<String, Object> map, int order) {
      return super.createPropertySource("cloudfoundry-vcap-services", map, order);
   }
}
