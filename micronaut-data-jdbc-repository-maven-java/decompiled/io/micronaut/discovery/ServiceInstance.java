package io.micronaut.discovery;

import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.StringUtils;
import io.micronaut.health.HealthStatus;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface ServiceInstance {
   String GROUP = "group";
   String ZONE = "zone";
   String REGION = "region";

   String getId();

   URI getURI();

   default HealthStatus getHealthStatus() {
      return HealthStatus.UP;
   }

   default Optional<String> getInstanceId() {
      return Optional.empty();
   }

   default Optional<String> getZone() {
      return this.getMetadata().get("zone", String.class);
   }

   default Optional<String> getRegion() {
      return this.getMetadata().get("region", String.class);
   }

   default Optional<String> getGroup() {
      return this.getMetadata().get("group", String.class);
   }

   default ConvertibleValues<String> getMetadata() {
      return ConvertibleValues.empty();
   }

   default String getHost() {
      return this.getURI().getHost();
   }

   default boolean isSecure() {
      String scheme = this.getURI().getScheme();
      return scheme != null && scheme.equalsIgnoreCase("https");
   }

   default int getPort() {
      return this.getURI().getPort();
   }

   default URI resolve(URI relativeURI) {
      URI thisUri = this.getURI();
      if (StringUtils.isNotEmpty(thisUri.getUserInfo())) {
         try {
            thisUri = new URI(thisUri.getScheme(), null, thisUri.getHost(), thisUri.getPort(), thisUri.getPath(), thisUri.getQuery(), thisUri.getFragment());
         } catch (URISyntaxException var4) {
            throw new IllegalStateException("ServiceInstance URI is invalid: " + var4.getMessage(), var4);
         }
      }

      String rawQuery = thisUri.getRawQuery();
      return StringUtils.isNotEmpty(rawQuery) ? thisUri.resolve(relativeURI + "?" + rawQuery) : thisUri.resolve(relativeURI);
   }

   static ServiceInstance of(String id, URL url) {
      try {
         URI uri = url.toURI();
         return of(id, uri);
      } catch (URISyntaxException var3) {
         throw new IllegalArgumentException("Invalid URI argument: " + url);
      }
   }

   static ServiceInstance of(String id, URI uri) {
      return new ServiceInstance() {
         @Override
         public String getId() {
            return id;
         }

         @Override
         public URI getURI() {
            return uri;
         }

         @Override
         public ConvertibleValues<String> getMetadata() {
            String userInfo = uri.getUserInfo();
            if (userInfo == null) {
               return ServiceInstance.super.getMetadata();
            } else {
               Map<String, String> metadata = new HashMap(1);
               metadata.put("Authorization-Info", userInfo);
               return ConvertibleValues.of(metadata);
            }
         }
      };
   }

   static ServiceInstance of(String id, String host, int port) {
      return new ServiceInstance() {
         @Override
         public String getId() {
            return id;
         }

         @Override
         public URI getURI() {
            return URI.create("http://" + host + ":" + port);
         }
      };
   }

   static ServiceInstance.Builder builder(String id, URI uri) {
      return new DefaultServiceInstance(id, uri);
   }

   public interface Builder {
      ServiceInstance.Builder instanceId(String id);

      ServiceInstance.Builder zone(String zone);

      ServiceInstance.Builder region(String region);

      ServiceInstance.Builder group(String group);

      ServiceInstance.Builder status(HealthStatus status);

      ServiceInstance.Builder metadata(Map<String, String> metadata);

      ServiceInstance build();
   }
}
