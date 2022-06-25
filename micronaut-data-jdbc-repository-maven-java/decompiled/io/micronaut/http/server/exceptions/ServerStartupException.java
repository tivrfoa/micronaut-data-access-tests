package io.micronaut.http.server.exceptions;

public class ServerStartupException extends HttpServerException {
   public ServerStartupException(String message) {
      super(message);
   }

   public ServerStartupException(String message, Throwable cause) {
      super(message, cause);
   }
}
