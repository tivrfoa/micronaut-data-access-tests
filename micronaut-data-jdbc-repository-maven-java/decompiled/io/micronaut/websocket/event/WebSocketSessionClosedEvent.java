package io.micronaut.websocket.event;

import io.micronaut.websocket.WebSocketSession;

public class WebSocketSessionClosedEvent extends WebSocketEvent {
   public WebSocketSessionClosedEvent(WebSocketSession session) {
      super(session);
   }
}
