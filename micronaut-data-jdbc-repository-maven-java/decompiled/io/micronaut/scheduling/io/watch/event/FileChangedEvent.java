package io.micronaut.scheduling.io.watch.event;

import io.micronaut.context.event.ApplicationEvent;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

public class FileChangedEvent extends ApplicationEvent {
   private final Path path;
   private final WatchEventType eventType;

   public FileChangedEvent(@NonNull Path path, @NonNull WatchEventType eventType) {
      super(path);
      ArgumentUtils.requireNonNull("path", path);
      ArgumentUtils.requireNonNull("eventType", eventType);
      this.path = path;
      this.eventType = eventType;
   }

   public FileChangedEvent(@NonNull Path path, @NonNull Kind eventType) {
      this(path, WatchEventType.of(eventType));
   }

   @NonNull
   public Path getSource() {
      return (Path)super.getSource();
   }

   @NonNull
   public Path getPath() {
      return this.path;
   }

   @NonNull
   public WatchEventType getEventType() {
      return this.eventType;
   }
}
