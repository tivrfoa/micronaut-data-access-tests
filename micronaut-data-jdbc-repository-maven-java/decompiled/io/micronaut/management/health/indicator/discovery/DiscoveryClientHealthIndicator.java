package io.micronaut.management.health.indicator.discovery;

import io.micronaut.context.annotation.Requires;
import io.micronaut.discovery.DiscoveryClient;
import io.micronaut.discovery.ServiceInstance;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Requires(
   beans = {DiscoveryClient.class, DiscoveryClientHealthIndicatorConfiguration.class}
)
@Singleton
public class DiscoveryClientHealthIndicator implements HealthIndicator {
   private final DiscoveryClient discoveryClient;

   public DiscoveryClientHealthIndicator(DiscoveryClient discoveryClient) {
      this.discoveryClient = discoveryClient;
   }

   @Override
   public Publisher<HealthResult> getResult() {
      return Flux.from(this.discoveryClient.getServiceIds())
         .<HealthResult>flatMap(
            ids -> {
               List<Flux<Map<String, List<ServiceInstance>>>> serviceMap = (List)ids.stream().map(id -> {
                  Flux<List<ServiceInstance>> serviceList = Flux.from(this.discoveryClient.getInstances(id));
                  return serviceList.map(serviceInstances -> Collections.singletonMap(id, serviceInstances));
               }).collect(Collectors.toList());
               Flux<Map<String, List<ServiceInstance>>> mergedServiceMap = Flux.merge(serviceMap);
               return mergedServiceMap.reduce(new LinkedHashMap(), (allServiceMap, service) -> {
                     allServiceMap.putAll(service);
                     return allServiceMap;
                  })
                  .map(
                     details -> {
                        HealthResult.Builder builder = HealthResult.builder(this.discoveryClient.getDescription(), HealthStatus.UP);
                        Stream<Entry<String, List<ServiceInstance>>> entryStream = details.entrySet().stream();
                        Map<String, Object> value = (Map)entryStream.collect(
                           Collectors.toMap(
                              Entry::getKey, entry -> (List)((List)entry.getValue()).stream().map(ServiceInstance::getURI).collect(Collectors.toList())
                           )
                        );
                        builder.details(Collections.singletonMap("services", value));
                        return builder.build();
                     }
                  )
                  .flux();
            }
         )
         .onErrorResume(throwable -> {
            HealthResult.Builder builder = HealthResult.builder(this.discoveryClient.getDescription(), HealthStatus.DOWN);
            builder.exception(throwable);
            return Flux.just(builder.build());
         });
   }
}
