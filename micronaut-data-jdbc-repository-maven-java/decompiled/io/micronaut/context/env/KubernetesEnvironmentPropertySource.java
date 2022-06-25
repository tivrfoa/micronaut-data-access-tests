package io.micronaut.context.env;

import io.micronaut.core.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KubernetesEnvironmentPropertySource extends MapPropertySource {
   public static final String NAME = "k8s-env";
   static final List<String> VAR_SUFFIXES = Arrays.asList(
      "_TCP", "_TCP_PORT", "_TCP_PROTO", "_TCP_ADDR", "_UDP_PORT", "_UDP_PROTO", "_UDP_ADDR", "_SERVICE_PORT", "_SERVICE_PORT_HTTP", "_SERVICE_HOST"
   );

   public KubernetesEnvironmentPropertySource() {
      super("k8s-env", EnvironmentPropertySource.getEnv(getEnvNoK8s(), null, null));
   }

   public KubernetesEnvironmentPropertySource(@Nullable List<String> includes, @Nullable List<String> excludes) {
      super("k8s-env", EnvironmentPropertySource.getEnv(getEnvNoK8s(), includes, excludes));
   }

   @Override
   public int getOrder() {
      return -200;
   }

   @Override
   public PropertySource.PropertyConvention getConvention() {
      return PropertySource.PropertyConvention.ENVIRONMENT_VARIABLE;
   }

   static Map<String, String> getEnvNoK8s() {
      Map<String, String> props = new HashMap(CachedEnvironment.getenv());
      props.entrySet().removeIf(entry -> VAR_SUFFIXES.stream().anyMatch(s -> ((String)entry.getKey()).endsWith(s)));
      props.entrySet().removeIf(entry -> ((String)entry.getKey()).endsWith("_PORT") && ((String)entry.getValue()).startsWith("tcp://"));
      return props;
   }
}
