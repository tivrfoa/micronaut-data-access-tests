package io.micronaut.websocket.event;

import io.micronaut.websocket.WebSocketSession;

public class WebSocketSessionOpenEvent extends WebSocketEvent {
   public WebSocketSessionOpenEvent(WebSocketSession session) {
      super(session);
   }
}
