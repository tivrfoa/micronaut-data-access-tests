package io.micronaut.http.client;

import java.util.Optional;

public interface LoadBalancerResolver {
   Optional<? extends LoadBalancer> resolve(String... serviceReferences);
}
