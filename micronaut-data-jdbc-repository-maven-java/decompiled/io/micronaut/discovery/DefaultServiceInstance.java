package io.micronaut.discovery;

import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.StringUtils;
import io.micronaut.health.HealthStatus;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;

class DefaultServiceInstance implements ServiceInstance, ServiceInstance.Builder {
   private final String id;
   private final URI uri;
   private String instanceId;
   private String zone;
   private String region;
   private String group;
   private HealthStatus status = HealthStatus.UP;
   private ConvertibleValues<String> metadata = ConvertibleValues.empty();

   DefaultServiceInstance(String id, URI uri) {
      this.id = id;
      String userInfo = uri.getUserInfo();
      if (StringUtils.isNotEmpty(userInfo)) {
         try {
            this.uri = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
            this.metadata = ConvertibleValues.of(Collections.singletonMap("Authorization-Info", userInfo));
         } catch (URISyntaxException var5) {
            throw new IllegalStateException("ServiceInstance URI is invalid: " + var5.getMessage(), var5);
         }
      } else {
         this.uri = uri;
      }

   }

   @Override
   public String getId() {
      return this.id;
   }

   @Override
   public URI getURI() {
      return this.uri;
   }

   @Override
   public HealthStatus getHealthStatus() {
      return this.status;
   }

   @Override
   public Optional<String> getInstanceId() {
      return Optional.ofNullable(this.instanceId);
   }

   @Override
   public Optional<String> getZone() {
      return Optional.ofNullable(this.zone);
   }

   @Override
   public Optional<String> getRegion() {
      return Optional.ofNullable(this.region);
   }

   @Override
   public Optional<String> getGroup() {
      return Optional.ofNullable(this.group);
   }

   @Override
   public ConvertibleValues<String> getMetadata() {
      return this.metadata;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DefaultServiceInstance that = (DefaultServiceInstance)o;
         return Objects.equals(this.id, that.id) && Objects.equals(this.uri, that.uri);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.id, this.uri});
   }

   @Override
   public ServiceInstance.Builder instanceId(String id) {
      this.instanceId = id;
      return this;
   }

   @Override
   public ServiceInstance.Builder zone(String zone) {
      this.zone = zone;
      return this;
   }

   @Override
   public ServiceInstance.Builder region(String region) {
      this.region = region;
      return this;
   }

   @Override
   public ServiceInstance.Builder group(String group) {
      this.group = group;
      return this;
   }

   @Override
   public ServiceInstance.Builder status(HealthStatus status) {
      if (status != null) {
         this.status = status;
      }

      return this;
   }

   @Override
   public ServiceInstance.Builder metadata(Map<String, String> metadata) {
      if (metadata != null) {
         if (this.metadata == ConvertibleValues.EMPTY) {
            this.metadata = ConvertibleValues.of(metadata);
         } else {
            Map<String, String> newMetadata = new LinkedHashMap();

            for(Entry<String, String> entry : this.metadata) {
               newMetadata.put(entry.getKey(), entry.getValue());
            }

            newMetadata.putAll(metadata);
            this.metadata = ConvertibleValues.of(newMetadata);
         }
      }

      return this;
   }

   @Override
   public ServiceInstance build() {
      return this;
   }

   public String toString() {
      return this.getURI().toString() + " (" + this.getId() + ")";
   }
}
