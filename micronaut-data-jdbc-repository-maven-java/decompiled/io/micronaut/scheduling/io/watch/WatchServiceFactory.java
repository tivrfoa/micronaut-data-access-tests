package io.micronaut.scheduling.io.watch;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.CachedEnvironment;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Requirements({@Requires(
   property = "micronaut.io.watch.enabled",
   value = "true",
   defaultValue = "true"
), @Requires(
   condition = FileWatchCondition.class
), @Requires(
   missingClasses = {"io.methvin.watchservice.MacOSXListeningWatchService"}
)})
@Factory
public class WatchServiceFactory {
   protected static final Logger LOG = LoggerFactory.getLogger(WatchServiceFactory.class);

   @Bean(
      preDestroy = "close"
   )
   @Prototype
   @Requirements({@Requires(
   missingClasses = {"io.methvin.watchservice.MacOSXListeningWatchService"}
), @Requires(
   property = "micronaut.io.watch.enabled",
   value = "true",
   defaultValue = "true"
), @Requires(
   property = "micronaut.io.watch.paths"
)})
   @Primary
   public WatchService watchService() throws IOException {
      String name = CachedEnvironment.getProperty("os.name").toLowerCase();
      boolean isMacOS = "Mac OS X".equalsIgnoreCase(name) || "Darwin".equalsIgnoreCase(name);
      if (isMacOS) {
         LOG.warn("Using default File WatchService on OS X is slow. Consider adding 'io.micronaut:micronaut-runtime-osx' dependencies to use native file watch");
      }

      return FileSystems.getDefault().newWatchService();
   }
}
