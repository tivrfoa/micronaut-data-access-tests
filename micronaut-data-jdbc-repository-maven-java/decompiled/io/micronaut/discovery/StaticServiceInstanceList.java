package io.micronaut.discovery;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.health.HealthStatus;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StaticServiceInstanceList implements ServiceInstanceList {
   private final String id;
   private final Collection<URI> loadBalancedURIs;
   private final String contextPath;

   public StaticServiceInstanceList(String id, Collection<URI> loadBalancedURIs) {
      this(id, loadBalancedURIs, null);
   }

   public StaticServiceInstanceList(String id, Collection<URI> loadBalancedURIs, @Nullable String contextPath) {
      this.id = id;
      this.loadBalancedURIs = loadBalancedURIs;
      this.contextPath = contextPath;
   }

   @Override
   public String getID() {
      return this.id;
   }

   @Override
   public List<ServiceInstance> getInstances() {
      return (List<ServiceInstance>)this.loadBalancedURIs.stream().map(url -> {
         ServiceInstance.Builder builder = ServiceInstance.builder(this.id, url);
         builder.status(HealthStatus.UP);
         return builder.build();
      }).collect(Collectors.toList());
   }

   public Collection<URI> getLoadBalancedURIs() {
      return this.loadBalancedURIs;
   }

   @Override
   public Optional<String> getContextPath() {
      return Optional.ofNullable(this.contextPath);
   }
}
