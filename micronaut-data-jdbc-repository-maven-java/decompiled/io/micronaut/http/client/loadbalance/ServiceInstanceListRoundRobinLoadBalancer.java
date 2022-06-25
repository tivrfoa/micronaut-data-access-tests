package io.micronaut.http.client.loadbalance;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.discovery.ServiceInstance;
import io.micronaut.discovery.ServiceInstanceList;
import java.util.Optional;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class ServiceInstanceListRoundRobinLoadBalancer extends AbstractRoundRobinLoadBalancer {
   private final ServiceInstanceList serviceInstanceList;

   public ServiceInstanceListRoundRobinLoadBalancer(ServiceInstanceList serviceInstanceList) {
      this.serviceInstanceList = serviceInstanceList;
   }

   @Override
   public Publisher<ServiceInstance> select(@Nullable Object discriminator) {
      return Mono.fromCallable(() -> this.getNextAvailable(this.serviceInstanceList.getInstances()));
   }

   @Override
   public String getServiceID() {
      return this.serviceInstanceList.getID();
   }

   @Override
   public Optional<String> getContextPath() {
      return this.serviceInstanceList.getContextPath();
   }
}
