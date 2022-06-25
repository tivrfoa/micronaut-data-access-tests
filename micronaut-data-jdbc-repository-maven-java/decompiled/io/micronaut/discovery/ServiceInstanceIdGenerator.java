package io.micronaut.discovery;

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.NonNull;

public interface ServiceInstanceIdGenerator {
   @NonNull
   String generateId(Environment environment, ServiceInstance serviceInstance);
}
