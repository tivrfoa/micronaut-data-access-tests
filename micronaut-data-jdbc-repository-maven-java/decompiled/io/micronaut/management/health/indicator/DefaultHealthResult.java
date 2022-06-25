package io.micronaut.management.health.indicator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.health.HealthStatus;
import java.util.Map;

@Introspected
@ReflectiveAccess
class DefaultHealthResult implements HealthResult {
   private final String name;
   private final HealthStatus healthStatus;
   private final Object details;

   DefaultHealthResult(String name, HealthStatus healthStatus, Object details) {
      this.name = name;
      this.healthStatus = healthStatus;
      this.details = details;
   }

   @JsonCreator
   DefaultHealthResult(@JsonProperty("name") String name, @JsonProperty("status") String status, @JsonProperty("details") Map<String, Object> details) {
      this.name = name;
      switch(status) {
         case "DOWN":
            this.healthStatus = HealthStatus.DOWN;
            break;
         case "UP":
            this.healthStatus = HealthStatus.UP;
            break;
         default:
            this.healthStatus = new HealthStatus(status);
      }

      this.details = details;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public HealthStatus getStatus() {
      return this.healthStatus;
   }

   @Override
   public Object getDetails() {
      return this.details;
   }
}
