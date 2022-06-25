package io.micronaut.http.client.loadbalance;

import io.micronaut.discovery.DiscoveryClient;
import io.micronaut.http.client.LoadBalancer;
import jakarta.inject.Singleton;

@Singleton
public class DiscoveryClientLoadBalancerFactory {
   private final DiscoveryClient discoveryClient;

   public DiscoveryClientLoadBalancerFactory(DiscoveryClient discoveryClient) {
      this.discoveryClient = discoveryClient;
   }

   public LoadBalancer create(String serviceID) {
      return new DiscoveryClientRoundRobinLoadBalancer(serviceID, this.discoveryClient);
   }

   public DiscoveryClient getDiscoveryClient() {
      return this.discoveryClient;
   }
}
