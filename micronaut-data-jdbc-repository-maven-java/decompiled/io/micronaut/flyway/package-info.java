@Requires(
   property = "flyway.enabled",
   notEquals = "false",
   defaultValue = "true"
)
@Configuration
package io.micronaut.flyway;

import io.micronaut.context.annotation.Configuration;
import io.micronaut.context.annotation.Requires;
