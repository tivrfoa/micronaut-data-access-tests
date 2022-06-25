package io.micronaut.context.env;

import io.micronaut.core.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EnvironmentPropertySource extends MapPropertySource {
   public static final int POSITION = -200;
   public static final String NAME = "env";

   public EnvironmentPropertySource() {
      super("env", getEnv(null, null));
   }

   public EnvironmentPropertySource(@Nullable List<String> includes, @Nullable List<String> excludes) {
      super("env", getEnv(includes, excludes));
   }

   @Override
   public int getOrder() {
      return -200;
   }

   @Override
   public PropertySource.PropertyConvention getConvention() {
      return PropertySource.PropertyConvention.ENVIRONMENT_VARIABLE;
   }

   static Map getEnv(@Nullable List<String> includes, @Nullable List<String> excludes) {
      return getEnv(CachedEnvironment.getenv(), includes, excludes);
   }

   static Map getEnv(Map<String, String> env, @Nullable List<String> includes, @Nullable List<String> excludes) {
      if (includes == null && excludes == null) {
         return env;
      } else {
         Map<String, String> result = new HashMap();

         for(Entry<String, String> entry : env.entrySet()) {
            String envVar = (String)entry.getKey();
            if ((excludes == null || !excludes.contains(envVar)) && (includes == null || includes.contains(envVar))) {
               result.put(envVar, entry.getValue());
            }
         }

         return result;
      }
   }
}
