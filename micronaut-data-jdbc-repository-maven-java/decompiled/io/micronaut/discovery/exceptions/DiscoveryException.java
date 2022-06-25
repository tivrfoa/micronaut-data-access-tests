package io.micronaut.discovery.exceptions;

public class DiscoveryException extends RuntimeException {
   public DiscoveryException(String message) {
      super(message);
   }

   public DiscoveryException(String message, Throwable cause) {
      super(message, cause);
   }
}
