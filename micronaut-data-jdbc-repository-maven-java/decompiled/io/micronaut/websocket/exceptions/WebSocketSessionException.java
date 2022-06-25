package io.micronaut.websocket.exceptions;

public class WebSocketSessionException extends WebSocketException {
   public WebSocketSessionException(String message) {
      super(message);
   }

   public WebSocketSessionException(String message, Throwable cause) {
      super(message, cause);
   }
}
