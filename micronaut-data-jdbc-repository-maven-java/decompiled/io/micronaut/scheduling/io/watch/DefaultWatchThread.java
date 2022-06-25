package io.micronaut.scheduling.io.watch;

import io.micronaut.context.LifeCycle;
import io.micronaut.context.annotation.Parallel;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.scheduling.io.watch.event.FileChangedEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Requirements({@Requires(
   property = "micronaut.io.watch.paths"
), @Requires(
   property = "micronaut.io.watch.enabled",
   value = "true",
   defaultValue = "false"
), @Requires(
   condition = FileWatchCondition.class
), @Requires(
   notEnv = {"function", "android"}
), @Requires(
   beans = {WatchService.class}
)})
@Parallel
@Singleton
public class DefaultWatchThread implements LifeCycle<DefaultWatchThread> {
   private static final Logger LOG = LoggerFactory.getLogger(DefaultWatchThread.class);
   private final FileWatchConfiguration configuration;
   private final AtomicBoolean active = new AtomicBoolean(true);
   private final ApplicationEventPublisher eventPublisher;
   private final WatchService watchService;
   private Collection<WatchKey> watchKeys = new ConcurrentLinkedQueue();

   protected DefaultWatchThread(ApplicationEventPublisher eventPublisher, FileWatchConfiguration configuration, WatchService watchService) {
      this.eventPublisher = eventPublisher;
      this.configuration = configuration;
      this.watchService = watchService;
   }

   @Override
   public boolean isRunning() {
      return this.active.get();
   }

   @PostConstruct
   public DefaultWatchThread start() {
      try {
         List<Path> paths = this.configuration.getPaths();
         if (!paths.isEmpty()) {
            for(Path path : paths) {
               if (path.toFile().exists()) {
                  this.addWatchDirectory(path);
               }
            }
         }

         if (!this.watchKeys.isEmpty()) {
            new Thread(() -> {
               while(this.active.get()) {
                  try {
                     WatchKey watchKey = this.watchService.poll(this.configuration.getCheckInterval().toMillis(), TimeUnit.MILLISECONDS);
                     if (watchKey != null && this.watchKeys.contains(watchKey)) {
                        for(WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                           Kind<?> kind = watchEvent.kind();
                           if (kind == StandardWatchEventKinds.OVERFLOW) {
                              if (LOG.isWarnEnabled()) {
                                 LOG.warn("WatchService Overflow occurred");
                              }
                           } else {
                              Object context = watchEvent.context();
                              if (context instanceof Path) {
                                 if (LOG.isDebugEnabled()) {
                                    LOG.debug("File at path {} changed. Firing change event: {}", context, kind);
                                 }

                                 this.eventPublisher.publishEvent(new FileChangedEvent((Path)context, kind));
                              }
                           }
                        }

                        watchKey.reset();
                     }
                  } catch (ClosedWatchServiceException | InterruptedException var7) {
                  }
               }

            }, "micronaut-filewatch-thread").start();
         }
      } catch (IOException var4) {
         if (LOG.isErrorEnabled()) {
            LOG.error("Error starting file watch service: " + var4.getMessage(), var4);
         }
      }

      return this;
   }

   public DefaultWatchThread stop() {
      this.active.set(false);
      this.closeWatchService();
      return this;
   }

   @PreDestroy
   @Override
   public void close() {
      this.stop();
   }

   @NonNull
   public WatchService getWatchService() {
      return this.watchService;
   }

   protected void closeWatchService() {
      try {
         this.getWatchService().close();
      } catch (IOException var2) {
         if (LOG.isErrorEnabled()) {
            LOG.error("Error stopping file watch service: " + var2.getMessage(), var2);
         }
      }

   }

   @NonNull
   protected WatchKey registerPath(@NonNull Path dir) throws IOException {
      return dir.register(this.watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
   }

   private boolean isValidDirectoryToMonitor(File file) {
      return file.isDirectory() && !file.isHidden() && !file.getName().startsWith(".");
   }

   private Path addWatchDirectory(Path p) throws IOException {
      return Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (!DefaultWatchThread.this.isValidDirectoryToMonitor(dir.toFile())) {
               return FileVisitResult.SKIP_SUBTREE;
            } else {
               WatchKey watchKey = DefaultWatchThread.this.registerPath(dir);
               DefaultWatchThread.this.watchKeys.add(watchKey);
               return FileVisitResult.CONTINUE;
            }
         }
      });
   }
}
