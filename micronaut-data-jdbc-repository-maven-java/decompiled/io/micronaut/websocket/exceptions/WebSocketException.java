package io.micronaut.websocket.exceptions;

public class WebSocketException extends RuntimeException {
   public WebSocketException(String message) {
      super(message);
   }

   public WebSocketException(String message, Throwable cause) {
      super(message, cause);
   }
}
