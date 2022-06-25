package io.micronaut.http.server.exceptions;

public class InternalServerException extends HttpServerException {
   public InternalServerException(String message) {
      super(message);
   }

   public InternalServerException(String message, Throwable cause) {
      super(message, cause);
   }
}
