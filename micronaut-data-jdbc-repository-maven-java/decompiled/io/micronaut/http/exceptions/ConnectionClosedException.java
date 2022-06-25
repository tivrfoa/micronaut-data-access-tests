package io.micronaut.http.exceptions;

public class ConnectionClosedException extends HttpException {
   public ConnectionClosedException(String message) {
      super(message);
   }

   public ConnectionClosedException(String message, Throwable cause) {
      super(message, cause);
   }
}
