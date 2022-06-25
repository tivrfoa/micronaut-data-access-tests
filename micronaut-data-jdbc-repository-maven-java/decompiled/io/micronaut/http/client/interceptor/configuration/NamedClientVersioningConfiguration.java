package io.micronaut.http.client.interceptor.configuration;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;

@EachProperty(
   value = "micronaut.http.client.versioning",
   primary = "default"
)
public class NamedClientVersioningConfiguration extends ClientVersioningConfiguration {
   NamedClientVersioningConfiguration(@Parameter String clientName) {
      super(clientName);
   }
}
