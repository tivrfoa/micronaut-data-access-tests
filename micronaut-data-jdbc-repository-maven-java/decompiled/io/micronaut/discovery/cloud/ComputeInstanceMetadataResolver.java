package io.micronaut.discovery.cloud;

import io.micronaut.context.env.Environment;
import java.util.Optional;

public interface ComputeInstanceMetadataResolver {
   Optional<ComputeInstanceMetadata> resolve(Environment environment);
}
