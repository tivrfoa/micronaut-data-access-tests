package io.micronaut.runtime.server.event;

import io.micronaut.runtime.event.ApplicationStartupEvent;
import io.micronaut.runtime.server.EmbeddedServer;

public class ServerStartupEvent extends ApplicationStartupEvent {
   public ServerStartupEvent(EmbeddedServer embeddedServer) {
      super(embeddedServer);
   }

   public EmbeddedServer getSource() {
      return (EmbeddedServer)super.getSource();
   }
}
