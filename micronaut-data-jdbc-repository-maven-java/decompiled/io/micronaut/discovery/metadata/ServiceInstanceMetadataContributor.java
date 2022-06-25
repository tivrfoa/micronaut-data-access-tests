package io.micronaut.discovery.metadata;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.discovery.ServiceInstance;
import java.util.Map;

@Indexed(ServiceInstanceMetadataContributor.class)
public interface ServiceInstanceMetadataContributor {
   void contribute(ServiceInstance instance, Map<String, String> metadata);
}
