package io.micronaut.scheduling.io.watch;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.Toggleable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties("micronaut.io.watch")
@Requires(
   property = "micronaut.io.watch.paths"
)
public class FileWatchConfiguration implements Toggleable {
   public static final String PREFIX = "micronaut.io.watch";
   public static final String PATHS = "micronaut.io.watch.paths";
   public static final String ENABLED = "micronaut.io.watch.enabled";
   public static final String RESTART = "micronaut.io.watch.restart";
   private boolean enabled = true;
   private boolean restart = false;
   private List<Path> paths = Collections.singletonList(Paths.get("src/main"));
   private Duration checkInterval = Duration.ofMillis(300L);

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean isRestart() {
      return this.restart;
   }

   public void setRestart(boolean restart) {
      this.restart = restart;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public List<Path> getPaths() {
      return this.paths;
   }

   public void setPaths(@NonNull List<Path> paths) {
      ArgumentUtils.requireNonNull("paths", paths);
      this.paths = paths;
   }

   @NonNull
   public Duration getCheckInterval() {
      return this.checkInterval;
   }

   public void setCheckInterval(@NonNull Duration checkInterval) {
      ArgumentUtils.requireNonNull("checkInterval", checkInterval);
      this.checkInterval = checkInterval;
   }
}
