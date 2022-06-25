package io.micronaut.http.client.loadbalance;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.discovery.DiscoveryClient;
import io.micronaut.discovery.ServiceInstance;
import org.reactivestreams.Publisher;

public class DiscoveryClientRoundRobinLoadBalancer extends AbstractRoundRobinLoadBalancer {
   private final String serviceID;
   private final DiscoveryClient discoveryClient;

   public DiscoveryClientRoundRobinLoadBalancer(String serviceID, DiscoveryClient discoveryClient) {
      this.serviceID = serviceID;
      this.discoveryClient = discoveryClient;
   }

   @Override
   public String getServiceID() {
      return this.serviceID;
   }

   @Override
   public Publisher<ServiceInstance> select(Object discriminator) {
      return Publishers.map(this.discoveryClient.getInstances(this.serviceID), this::getNextAvailable);
   }
}
