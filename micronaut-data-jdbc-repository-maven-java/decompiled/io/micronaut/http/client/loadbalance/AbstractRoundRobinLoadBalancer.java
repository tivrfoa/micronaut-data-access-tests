package io.micronaut.http.client.loadbalance;

import io.micronaut.discovery.ServiceInstance;
import io.micronaut.discovery.exceptions.NoAvailableServiceException;
import io.micronaut.health.HealthStatus;
import io.micronaut.http.client.LoadBalancer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class AbstractRoundRobinLoadBalancer implements LoadBalancer {
   protected final AtomicInteger index = new AtomicInteger(0);

   public abstract String getServiceID();

   protected ServiceInstance getNextAvailable(List<ServiceInstance> serviceInstances) {
      List<ServiceInstance> availableServices = (List)serviceInstances.stream()
         .filter(si -> si.getHealthStatus().equals(HealthStatus.UP))
         .collect(Collectors.toList());
      int len = availableServices.size();
      if (len == 0) {
         throw new NoAvailableServiceException(this.getServiceID());
      } else {
         int i = this.getServiceIndex(len);

         try {
            return (ServiceInstance)availableServices.get(i);
         } catch (IndexOutOfBoundsException var6) {
            this.index.set(0);
            i = this.getServiceIndex(len);
            return (ServiceInstance)availableServices.get(i);
         }
      }
   }

   private int getServiceIndex(int len) {
      return this.index.getAndAccumulate(len, (cur, n) -> cur >= n - 1 ? 0 : cur + 1);
   }
}
