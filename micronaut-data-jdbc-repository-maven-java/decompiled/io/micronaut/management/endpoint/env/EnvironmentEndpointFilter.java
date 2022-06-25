package io.micronaut.management.endpoint.env;

import javax.validation.constraints.NotNull;

public interface EnvironmentEndpointFilter {
   void specifyFiltering(@NotNull EnvironmentFilterSpecification specification);
}
