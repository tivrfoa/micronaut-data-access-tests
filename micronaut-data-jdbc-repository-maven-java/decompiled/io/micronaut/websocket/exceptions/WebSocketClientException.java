package io.micronaut.websocket.exceptions;

public class WebSocketClientException extends WebSocketException {
   public WebSocketClientException(String message) {
      super(message);
   }

   public WebSocketClientException(String message, Throwable cause) {
      super(message, cause);
   }
}
