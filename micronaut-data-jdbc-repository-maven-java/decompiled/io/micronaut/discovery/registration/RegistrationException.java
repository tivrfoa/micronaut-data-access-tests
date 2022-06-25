package io.micronaut.discovery.registration;

import io.micronaut.discovery.exceptions.DiscoveryException;

public class RegistrationException extends DiscoveryException {
   public RegistrationException(String message) {
      super(message);
   }

   public RegistrationException(String message, Throwable cause) {
      super(message, cause);
   }
}
