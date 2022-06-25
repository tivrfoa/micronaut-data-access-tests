package io.micronaut.inject.util;

import io.micronaut.context.env.CachedEnvironment;
import io.micronaut.core.annotation.Internal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;

@Internal
public class VisitorContextUtils {
   public static Map<String, String> getSystemOptions() {
      return (Map<String, String>)Optional.ofNullable(System.getProperties())
         .map(
            properties -> (Map)properties.stringPropertyNames()
                  .stream()
                  .filter(name -> name.startsWith("micronaut"))
                  .map(k -> new SimpleEntry(k, CachedEnvironment.getProperty(k)))
                  .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue))
         )
         .orElse(Collections.emptyMap());
   }

   public static Map<String, String> getProcessorOptions(ProcessingEnvironment processingEnv) {
      return (Map<String, String>)Optional.ofNullable(processingEnv)
         .map(ProcessingEnvironment::getOptions)
         .map(Map::entrySet)
         .map(Collection::stream)
         .map(
            entryStream -> (Map)entryStream.filter(e -> ((String)e.getKey()).startsWith("micronaut")).collect(Collectors.toMap(Entry::getKey, Entry::getValue))
         )
         .orElse(Collections.emptyMap());
   }
}
