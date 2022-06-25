package io.micronaut.scheduling.exceptions;

import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.MethodReference;

public class SchedulerConfigurationException extends ConfigurationException {
   public SchedulerConfigurationException(ExecutableMethod<?, ?> method, String message) {
      super("Invalid @Scheduled definition for method: " + method + " - Reason: " + message);
   }

   public SchedulerConfigurationException(MethodReference<?, ?> method, String message) {
      super("Invalid @Scheduled definition for method: " + method + " - Reason: " + message);
   }
}
