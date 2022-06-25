package io.micronaut.management.health.aggregator;

import io.micronaut.context.annotation.Requires;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.endpoint.health.HealthLevelOfDetail;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.runtime.ApplicationConfiguration;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
@Requires(
   beans = {HealthEndpoint.class}
)
public class DefaultHealthAggregator implements HealthAggregator<HealthResult> {
   private final ApplicationConfiguration applicationConfiguration;

   public DefaultHealthAggregator(ApplicationConfiguration applicationConfiguration) {
      this.applicationConfiguration = applicationConfiguration;
   }

   @Override
   public Publisher<HealthResult> aggregate(HealthIndicator[] indicators, HealthLevelOfDetail healthLevelOfDetail) {
      Flux<HealthResult> results = this.aggregateResults(indicators);
      Mono<HealthResult> result = results.collectList().map(list -> {
         HealthStatus overallStatus = this.calculateOverallStatus(list);
         return this.buildResult(overallStatus, this.aggregateDetails(list), healthLevelOfDetail);
      });
      return result.flux();
   }

   @Override
   public Publisher<HealthResult> aggregate(String name, Publisher<HealthResult> results) {
      Mono<HealthResult> result = Flux.from(results).collectList().map(list -> {
         HealthStatus overallStatus = this.calculateOverallStatus(list);
         Object details = this.aggregateDetails(list);
         return HealthResult.builder(name, overallStatus).details(details).build();
      });
      return result.flux();
   }

   protected HealthStatus calculateOverallStatus(List<HealthResult> results) {
      return (HealthStatus)results.stream().map(HealthResult::getStatus).sorted().distinct().reduce((a, b) -> b).orElse(HealthStatus.UNKNOWN);
   }

   protected Flux<HealthResult> aggregateResults(HealthIndicator[] indicators) {
      return Flux.merge(
         (Iterable<? extends Publisher<? extends HealthResult>>)Arrays.stream(indicators).map(HealthIndicator::getResult).collect(Collectors.toList())
      );
   }

   protected Object aggregateDetails(List<HealthResult> results) {
      Map<String, Object> details = new HashMap(results.size());
      results.forEach(r -> details.put(r.getName(), this.buildResult(r.getStatus(), r.getDetails(), HealthLevelOfDetail.STATUS_DESCRIPTION_DETAILS)));
      return details;
   }

   protected HealthResult buildResult(HealthStatus status, Object details, HealthLevelOfDetail healthLevelOfDetail) {
      return healthLevelOfDetail == HealthLevelOfDetail.STATUS
         ? HealthResult.builder(null, status).build()
         : HealthResult.builder((String)this.applicationConfiguration.getName().orElse("application"), status).details(details).build();
   }
}
