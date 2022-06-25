package io.micronaut.scheduling.io.watch.event;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;

public enum WatchEventType {
   CREATE,
   MODIFY,
   DELETE;

   public static WatchEventType of(Kind kind) {
      if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
         return CREATE;
      } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
         return MODIFY;
      } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
         return DELETE;
      } else {
         throw new IllegalArgumentException("Unsupported watch event kind: " + kind);
      }
   }
}
