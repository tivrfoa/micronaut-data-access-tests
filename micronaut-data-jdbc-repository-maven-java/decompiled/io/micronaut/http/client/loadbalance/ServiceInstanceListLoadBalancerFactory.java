package io.micronaut.http.client.loadbalance;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.discovery.ServiceInstanceList;
import io.micronaut.http.client.LoadBalancer;
import jakarta.inject.Singleton;

@Singleton
@BootstrapContextCompatible
public class ServiceInstanceListLoadBalancerFactory {
   public LoadBalancer create(ServiceInstanceList serviceInstanceList) {
      return new ServiceInstanceListRoundRobinLoadBalancer(serviceInstanceList);
   }
}
