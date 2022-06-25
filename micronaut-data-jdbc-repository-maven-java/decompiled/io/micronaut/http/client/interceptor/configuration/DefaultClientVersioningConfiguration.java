package io.micronaut.http.client.interceptor.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import java.util.Collections;

@ConfigurationProperties("micronaut.http.client.versioning.default")
@Primary
@Requires(
   missingProperty = "micronaut.http.client.versioning.default"
)
@Internal
public class DefaultClientVersioningConfiguration extends ClientVersioningConfiguration {
   public static final String DEFAULT_HEADER_NAME = "X-API-VERSION";
   public static final String DEFAULT_PARAMETER_NAME = "api-version";
   public static final String PREFIX = "micronaut.http.client.versioning.default";

   DefaultClientVersioningConfiguration() {
      super("default");
      this.setHeaders(Collections.singletonList("X-API-VERSION"));
   }
}
