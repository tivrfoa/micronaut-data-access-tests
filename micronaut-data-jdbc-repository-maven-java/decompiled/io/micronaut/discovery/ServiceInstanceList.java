package io.micronaut.discovery;

import java.util.List;
import java.util.Optional;

public interface ServiceInstanceList {
   String getID();

   List<ServiceInstance> getInstances();

   default Optional<String> getContextPath() {
      return Optional.empty();
   }
}
