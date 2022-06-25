package io.micronaut.http.client;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.discovery.ServiceInstance;
import io.micronaut.discovery.exceptions.NoAvailableServiceException;
import io.micronaut.http.client.loadbalance.FixedLoadBalancer;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import org.reactivestreams.Publisher;

@FunctionalInterface
public interface LoadBalancer {
   Publisher<ServiceInstance> select(@Nullable Object discriminator);

   default Optional<String> getContextPath() {
      return Optional.empty();
   }

   default Publisher<ServiceInstance> select() {
      return this.select(null);
   }

   @Deprecated
   static LoadBalancer fixed(URL url) {
      return new FixedLoadBalancer(url);
   }

   static LoadBalancer fixed(URI uri) {
      return new FixedLoadBalancer(uri);
   }

   static LoadBalancer empty() {
      return discriminator -> Publishers.just(new NoAvailableServiceException("Load balancer contains no servers"));
   }
}
