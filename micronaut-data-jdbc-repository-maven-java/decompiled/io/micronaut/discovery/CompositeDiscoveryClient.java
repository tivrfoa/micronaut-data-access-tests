package io.micronaut.discovery;

import io.micronaut.core.naming.Described;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.ArrayUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class CompositeDiscoveryClient implements DiscoveryClient {
   private final DiscoveryClient[] discoveryClients;

   protected CompositeDiscoveryClient(DiscoveryClient[] discoveryClients) {
      this.discoveryClients = discoveryClients;
   }

   @Override
   public String getDescription() {
      return this.toString();
   }

   public DiscoveryClient[] getDiscoveryClients() {
      return this.discoveryClients;
   }

   @Override
   public Publisher<List<ServiceInstance>> getInstances(String serviceId) {
      serviceId = NameUtils.hyphenate(serviceId);
      if (ArrayUtils.isEmpty(this.discoveryClients)) {
         return Flux.just(Collections.emptyList());
      } else {
         Mono<List<ServiceInstance>> reduced = Flux.fromArray(this.discoveryClients)
            .flatMap(client -> client.getInstances(serviceId))
            .reduce(new ArrayList(), (instances, otherInstances) -> {
               instances.addAll(otherInstances);
               return instances;
            });
         return reduced.flux();
      }
   }

   @Override
   public Publisher<List<String>> getServiceIds() {
      if (ArrayUtils.isEmpty(this.discoveryClients)) {
         return Flux.just(Collections.emptyList());
      } else {
         Mono<List<String>> reduced = Flux.fromArray(this.discoveryClients)
            .flatMap(DiscoveryClient::getServiceIds)
            .reduce(new ArrayList(), (serviceIds, otherServiceIds) -> {
               serviceIds.addAll(otherServiceIds);
               return serviceIds;
            });
         return reduced.flux();
      }
   }

   public void close() throws IOException {
      for(DiscoveryClient discoveryClient : this.discoveryClients) {
         discoveryClient.close();
      }

   }

   public String toString() {
      return "compositeDiscoveryClient(" + (String)Arrays.stream(this.discoveryClients).map(Described::getDescription).collect(Collectors.joining(",")) + ")";
   }
}
