package io.micronaut.websocket.event;

import io.micronaut.context.event.ApplicationEvent;
import io.micronaut.websocket.WebSocketSession;

public class WebSocketEvent extends ApplicationEvent {
   public WebSocketEvent(WebSocketSession session) {
      super(session);
   }

   public WebSocketSession getSource() {
      return (WebSocketSession)super.getSource();
   }
}
