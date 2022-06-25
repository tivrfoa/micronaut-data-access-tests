package io.micronaut.context.env;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.optim.StaticOptimizations;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Internal
public class CachedEnvironment {
   private static final boolean LOCKED = StaticOptimizations.isEnvironmentCached();
   private static final Map<String, String> CACHED_ENVIRONMENT;
   private static final Map<Object, String> CACHED_PROPERTIES;

   @Nullable
   public static String getenv(String name) {
      return LOCKED ? (String)CACHED_ENVIRONMENT.get(name) : System.getenv(name);
   }

   @NonNull
   public static Map<String, String> getenv() {
      return LOCKED ? CACHED_ENVIRONMENT : System.getenv();
   }

   @Nullable
   public static String getProperty(String name) {
      return LOCKED ? (String)CACHED_PROPERTIES.get(name) : System.getProperty(name);
   }

   static {
      if (LOCKED) {
         CACHED_ENVIRONMENT = Collections.unmodifiableMap(new HashMap(System.getenv()));
         Map<Object, String> props = new HashMap();
         System.getProperties().forEach((key, value) -> {
            String var10000 = (String)props.put(key, String.valueOf(value));
         });
         CACHED_PROPERTIES = Collections.unmodifiableMap(props);
      } else {
         CACHED_ENVIRONMENT = Collections.emptyMap();
         CACHED_PROPERTIES = Collections.emptyMap();
      }

   }
}
