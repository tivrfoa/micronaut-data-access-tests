package io.micronaut.runtime.server.event;

import io.micronaut.runtime.event.ApplicationShutdownEvent;
import io.micronaut.runtime.server.EmbeddedServer;

public class ServerShutdownEvent extends ApplicationShutdownEvent {
   public ServerShutdownEvent(EmbeddedServer embeddedServer) {
      super(embeddedServer);
   }

   public EmbeddedServer getSource() {
      return (EmbeddedServer)super.getSource();
   }
}
