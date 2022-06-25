package io.micronaut.context.env.yaml;

import io.micronaut.context.env.AbstractPropertySourceLoader;
import io.micronaut.core.util.CollectionUtils;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class YamlPropertySourceLoader extends AbstractPropertySourceLoader {
   private static final Logger LOG = LoggerFactory.getLogger(YamlPropertySourceLoader.class);

   @Override
   public boolean isEnabled() {
      return isSnakeYamlPresent();
   }

   @Override
   public Set<String> getExtensions() {
      return CollectionUtils.setOf("yml", "yaml");
   }

   @Override
   protected void processInput(String name, InputStream input, Map<String, Object> finalMap) {
      if (System.getProperty("java.runtime.name") == null) {
         System.setProperty("java.runtime.name", "Unknown");
      }

      Yaml yaml = new Yaml(new CustomSafeConstructor());
      Iterable<Object> objects = yaml.loadAll(input);
      Iterator<Object> i = objects.iterator();
      if (i.hasNext()) {
         while(i.hasNext()) {
            Object object = i.next();
            if (object instanceof Map) {
               Map map = (Map)object;
               if (LOG.isTraceEnabled()) {
                  LOG.trace("Processing YAML: {}", map);
               }

               String prefix = "";
               this.processMap(finalMap, map, prefix);
            }
         }
      } else if (LOG.isTraceEnabled()) {
         LOG.trace("PropertySource [{}] produced no YAML content", name);
      }

   }

   private static boolean isSnakeYamlPresent() {
      try {
         Class<Yaml> yamlClass = Yaml.class;
         return true;
      } catch (Throwable var1) {
         return false;
      }
   }
}
