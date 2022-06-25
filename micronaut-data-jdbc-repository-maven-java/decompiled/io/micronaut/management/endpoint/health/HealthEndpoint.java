package io.micronaut.management.endpoint.health;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.health.HealthStatus;
import io.micronaut.http.HttpStatus;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import io.micronaut.management.endpoint.annotation.Selector;
import io.micronaut.management.health.aggregator.HealthAggregator;
import io.micronaut.management.health.indicator.HealthCheckType;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.management.health.indicator.annotation.Liveness;
import jakarta.inject.Inject;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Endpoint(
   value = "health",
   defaultSensitive = false
)
public class HealthEndpoint {
   public static final boolean DEFAULT_SENSITIVE = false;
   public static final String NAME = "health";
   public static final String PREFIX = "endpoints.health";
   private HealthAggregator<HealthResult> healthAggregator;
   private HealthIndicator[] healthIndicators;
   private HealthIndicator[] livenessHealthIndicators;
   private HealthIndicator[] readinessHealthIndicators;
   private DetailsVisibility detailsVisible = DetailsVisibility.AUTHENTICATED;
   private HealthEndpoint.StatusConfiguration statusConfiguration;

   public HealthEndpoint(
      HealthAggregator<HealthResult> healthAggregator, HealthIndicator[] healthIndicators, @Liveness HealthIndicator[] livenessHealthIndicators
   ) {
      this.healthAggregator = healthAggregator;
      this.healthIndicators = healthIndicators;
      this.livenessHealthIndicators = livenessHealthIndicators;
      this.readinessHealthIndicators = this.getReadinessHealthIndicators(healthIndicators, livenessHealthIndicators);
   }

   protected final HealthIndicator[] getReadinessHealthIndicators(HealthIndicator[] allHealthIndicators, HealthIndicator[] livenessHealthIndicators) {
      List<HealthIndicator> liveness = Arrays.asList(livenessHealthIndicators);
      return (HealthIndicator[])Arrays.stream(allHealthIndicators)
         .filter(healthIndicator -> !liveness.contains(healthIndicator))
         .toArray(x$0 -> new HealthIndicator[x$0]);
   }

   @Read
   @SingleResult
   public Publisher<HealthResult> getHealth(@Nullable Principal principal) {
      HealthLevelOfDetail detail = this.levelOfDetail(principal);
      return Mono.from(this.healthAggregator.aggregate(this.healthIndicators, detail));
   }

   @Read
   @SingleResult
   public Publisher<HealthResult> getHealth(@Nullable Principal principal, @Selector HealthCheckType selector) {
      HealthLevelOfDetail detail = this.levelOfDetail(principal);
      HealthIndicator[] indicators;
      switch(selector) {
         case LIVENESS:
            indicators = this.livenessHealthIndicators;
            break;
         case READINESS:
         default:
            indicators = this.readinessHealthIndicators;
      }

      return Mono.from(this.healthAggregator.aggregate(indicators, detail));
   }

   public DetailsVisibility getDetailsVisible() {
      return this.detailsVisible;
   }

   public void setDetailsVisible(DetailsVisibility detailsVisible) {
      this.detailsVisible = detailsVisible;
   }

   public HealthEndpoint.StatusConfiguration getStatusConfiguration() {
      return this.statusConfiguration;
   }

   @Inject
   public void setStatusConfiguration(HealthEndpoint.StatusConfiguration statusConfiguration) {
      if (statusConfiguration != null) {
         this.statusConfiguration = statusConfiguration;
      }

   }

   protected HealthLevelOfDetail levelOfDetail(@Nullable Principal principal) {
      boolean showDetails = false;
      switch(this.detailsVisible) {
         case AUTHENTICATED:
            showDetails = principal != null;
            break;
         case ANONYMOUS:
            showDetails = true;
      }

      return showDetails ? HealthLevelOfDetail.STATUS_DESCRIPTION_DETAILS : HealthLevelOfDetail.STATUS;
   }

   @ConfigurationProperties("status")
   public static class StatusConfiguration {
      private Map<String, HttpStatus> httpMapping = new HashMap(5);

      public StatusConfiguration() {
         this.httpMapping.put("DOWN", HttpStatus.SERVICE_UNAVAILABLE);
         this.httpMapping.put("UP", HttpStatus.OK);
         this.httpMapping.put(HealthStatus.UNKNOWN.getName(), HttpStatus.OK);
      }

      public Map<String, HttpStatus> getHttpMapping() {
         return this.httpMapping;
      }

      public void setHttpMapping(Map<String, HttpStatus> httpMapping) {
         if (httpMapping != null) {
            for(Entry<String, HttpStatus> entry : httpMapping.entrySet()) {
               this.httpMapping.put(((String)entry.getKey()).toUpperCase(Locale.ENGLISH), entry.getValue());
            }
         }

      }
   }
}
