package io.micronaut.http.netty.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Nullable;
import io.netty.util.ResourceLeakDetector;

@ConfigurationProperties("netty")
public class NettyGlobalConfiguration {
   private ResourceLeakDetector.Level resourceLeakDetectorLevel;

   public void setResourceLeakDetectorLevel(ResourceLeakDetector.Level resourceLeakDetectorLevel) {
      this.resourceLeakDetectorLevel = resourceLeakDetectorLevel;
   }

   @Nullable
   public ResourceLeakDetector.Level getResourceLeakDetectorLevel() {
      return this.resourceLeakDetectorLevel;
   }
}
