package io.micronaut.http.client.exceptions;

public class NoHostException extends HttpClientException {
   public NoHostException(String message) {
      super(message);
   }

   public NoHostException(String message, Throwable cause) {
      super(message, cause);
   }

   public NoHostException(String message, Throwable cause, boolean shared) {
      super(message, cause, shared);
   }
}
