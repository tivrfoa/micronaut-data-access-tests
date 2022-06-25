package io.micronaut.discovery.exceptions;

public class NoAvailableServiceException extends DiscoveryException {
   private final String serviceID;

   public NoAvailableServiceException(String serviceID) {
      super("No available services for ID: " + serviceID);
      this.serviceID = serviceID;
   }

   public String getServiceID() {
      return this.serviceID;
   }
}
