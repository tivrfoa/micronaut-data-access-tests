package io.micronaut.management.health.indicator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.micronaut.health.HealthStatus;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonDeserialize(
   as = DefaultHealthResult.class
)
public interface HealthResult {
   String getName();

   HealthStatus getStatus();

   Object getDetails();

   static HealthResult.Builder builder(String name, HealthStatus status) {
      return new HealthResult.Builder(name, status);
   }

   static HealthResult.Builder builder(String name) {
      return new HealthResult.Builder(name);
   }

   public static class Builder {
      private static final Logger LOG = LoggerFactory.getLogger(HealthResult.class);
      private final String name;
      private HealthStatus status;
      private Object details;

      Builder(String name, HealthStatus status) {
         this.name = name;
         this.status = status;
      }

      Builder(String name) {
         this.name = name;
      }

      public HealthResult.Builder status(HealthStatus status) {
         this.status = status;
         return this;
      }

      public HealthResult.Builder exception(@NotNull Throwable ex) {
         Map<String, String> error = new HashMap(1);
         String message = ex.getClass().getName() + ": " + ex.getMessage();
         error.put("error", message);
         if (LOG.isErrorEnabled()) {
            LOG.error("Health indicator [" + this.name + "] reported exception: " + message, ex);
         }

         return this.details(error);
      }

      public HealthResult.Builder details(Object details) {
         this.details = details;
         return this;
      }

      public HealthResult build() {
         return new DefaultHealthResult(this.name, this.status != null ? this.status : HealthStatus.UNKNOWN, this.details);
      }
   }
}
