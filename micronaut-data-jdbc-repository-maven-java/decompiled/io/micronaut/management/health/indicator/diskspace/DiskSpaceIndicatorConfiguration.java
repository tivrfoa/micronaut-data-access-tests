package io.micronaut.management.health.indicator.diskspace;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.convert.format.ReadableBytes;
import io.micronaut.core.util.Toggleable;
import java.io.File;

@ConfigurationProperties("endpoints.health.disk-space")
public class DiskSpaceIndicatorConfiguration implements Toggleable {
   public static final boolean DEFAULT_ENABLED = true;
   public static final String DEFAULT_PATH = ".";
   public static final long DEFAULT_THRESHOLD = 10485760L;
   private boolean enabled = true;
   private File path = new File(".");
   private long threshold = 10485760L;

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   protected void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public File getPath() {
      return this.path;
   }

   protected void setPath(File path) {
      this.path = path;
   }

   public long getThreshold() {
      return this.threshold;
   }

   protected void setThreshold(@ReadableBytes long threshold) {
      this.threshold = threshold;
   }
}
