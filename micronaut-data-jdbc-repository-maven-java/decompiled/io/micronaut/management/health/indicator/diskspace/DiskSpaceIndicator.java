package io.micronaut.management.health.indicator.diskspace;

import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.AbstractHealthIndicator;
import jakarta.inject.Singleton;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
@Requirements({@Requires(
   property = "endpoints.health.disk-space.enabled",
   notEquals = "false"
), @Requires(
   beans = {HealthEndpoint.class}
)})
public class DiskSpaceIndicator extends AbstractHealthIndicator<Map<String, Object>> {
   protected static final String NAME = "diskSpace";
   private final DiskSpaceIndicatorConfiguration configuration;

   DiskSpaceIndicator(DiskSpaceIndicatorConfiguration configuration) {
      this.configuration = configuration;
   }

   @Override
   public String getName() {
      return "diskSpace";
   }

   protected Map<String, Object> getHealthInformation() {
      File path = this.configuration.getPath();
      long threshold = this.configuration.getThreshold();
      long freeSpace = path.getUsableSpace();
      Map<String, Object> detail = new LinkedHashMap(3);
      if (freeSpace >= threshold) {
         this.healthStatus = HealthStatus.UP;
         detail.put("total", path.getTotalSpace());
         detail.put("free", freeSpace);
         detail.put("threshold", threshold);
      } else {
         this.healthStatus = HealthStatus.DOWN;
         detail.put("error", String.format("Free disk space below threshold. Available: %d bytes (threshold: %d bytes)", freeSpace, threshold));
      }

      return detail;
   }
}
