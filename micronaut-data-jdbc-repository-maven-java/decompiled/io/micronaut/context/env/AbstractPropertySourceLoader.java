package io.micronaut.context.env;

import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.util.Toggleable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPropertySourceLoader implements PropertySourceLoader, Toggleable, Ordered {
   public static final int DEFAULT_POSITION = -300;
   private static final Logger LOG = LoggerFactory.getLogger(AbstractPropertySourceLoader.class);

   @Override
   public int getOrder() {
      return -300;
   }

   @Override
   public Optional<PropertySource> load(String resourceName, ResourceLoader resourceLoader) {
      return this.load(resourceLoader, resourceName, this.getOrder());
   }

   @Override
   public Optional<PropertySource> loadEnv(String resourceName, ResourceLoader resourceLoader, ActiveEnvironment activeEnvironment) {
      return this.load(resourceLoader, resourceName + "-" + activeEnvironment.getName(), this.getOrder() + 1 + activeEnvironment.getPriority());
   }

   private Optional<PropertySource> load(ResourceLoader resourceLoader, String fileName, int order) {
      if (this.isEnabled()) {
         for(String ext : this.getExtensions()) {
            String fileExt = fileName + "." + ext;
            Map<String, Object> finalMap = this.loadProperties(resourceLoader, fileName, fileExt);
            if (!finalMap.isEmpty()) {
               return Optional.of(this.createPropertySource(fileName, finalMap, order));
            }
         }
      }

      return Optional.empty();
   }

   protected MapPropertySource createPropertySource(String name, Map<String, Object> map, int order) {
      return new MapPropertySource(name, map) {
         @Override
         public int getOrder() {
            return order;
         }
      };
   }

   private Map<String, Object> loadProperties(ResourceLoader resourceLoader, String qualifiedName, String fileName) {
      Optional<InputStream> config = this.readInput(resourceLoader, fileName);
      if (!config.isPresent()) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("No PropertySource found for file name: " + fileName);
         }

         return Collections.emptyMap();
      } else {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Found PropertySource for file name: " + fileName);
         }

         try {
            InputStream input = (InputStream)config.get();
            Throwable var6 = null;

            Map var7;
            try {
               var7 = this.read(qualifiedName, input);
            } catch (Throwable var17) {
               var6 = var17;
               throw var17;
            } finally {
               if (input != null) {
                  if (var6 != null) {
                     try {
                        input.close();
                     } catch (Throwable var16) {
                        var6.addSuppressed(var16);
                     }
                  } else {
                     input.close();
                  }
               }

            }

            return var7;
         } catch (IOException var19) {
            throw new ConfigurationException("I/O exception occurred reading [" + fileName + "]: " + var19.getMessage(), var19);
         }
      }
   }

   @Override
   public Map<String, Object> read(String name, InputStream input) throws IOException {
      Map<String, Object> finalMap = new LinkedHashMap();
      this.processInput(name, input, finalMap);
      return finalMap;
   }

   protected Optional<InputStream> readInput(ResourceLoader resourceLoader, String fileName) {
      return resourceLoader.getResourceAsStream(fileName);
   }

   protected abstract void processInput(String name, InputStream input, Map<String, Object> finalMap) throws IOException;

   protected void processMap(Map<String, Object> finalMap, Map map, String prefix) {
      for(Object o : map.entrySet()) {
         Entry entry = (Entry)o;
         String key = entry.getKey().toString();
         Object value = entry.getValue();
         if (value instanceof Map && !((Map)value).isEmpty()) {
            this.processMap(finalMap, (Map)value, prefix + key + '.');
         } else {
            finalMap.put(prefix + key, value);
         }
      }

   }
}
