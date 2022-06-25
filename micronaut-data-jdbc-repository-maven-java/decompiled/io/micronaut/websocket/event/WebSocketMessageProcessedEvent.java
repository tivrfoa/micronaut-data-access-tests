package io.micronaut.websocket.event;

import io.micronaut.websocket.WebSocketSession;

public class WebSocketMessageProcessedEvent<T> extends WebSocketEvent {
   private T message;

   public WebSocketMessageProcessedEvent(WebSocketSession session, T message) {
      super(session);
      this.message = message;
   }

   public T getMessage() {
      return this.message;
   }
}
