package io.micronaut.runtime.server.watch.event;

import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.scheduling.io.watch.event.FileChangedEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requirements({@Requires(
   beans = {EmbeddedApplication.class}
), @Requires(
   property = "micronaut.io.watch.restart",
   value = "true",
   defaultValue = "false"
)})
public class FileWatchRestartListener implements ApplicationEventListener<FileChangedEvent> {
   private static final Logger LOG = LoggerFactory.getLogger(FileWatchRestartListener.class);
   private final EmbeddedApplication<?> embeddedApplication;

   @Inject
   public FileWatchRestartListener(EmbeddedApplication<?> embeddedApplication) {
      this.embeddedApplication = embeddedApplication;
   }

   @Deprecated
   public FileWatchRestartListener(EmbeddedServer embeddedServer) {
      this.embeddedApplication = embeddedServer;
   }

   public void onApplicationEvent(FileChangedEvent event) {
      this.embeddedApplication.stop();
      if (LOG.isInfoEnabled()) {
         LOG.info("Shutting down server following file change.");
      }

      System.exit(0);
   }

   public boolean supports(FileChangedEvent event) {
      return true;
   }
}
