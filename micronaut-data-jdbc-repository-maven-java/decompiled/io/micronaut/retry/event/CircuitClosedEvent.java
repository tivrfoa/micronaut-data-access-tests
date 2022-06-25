package io.micronaut.retry.event;

import io.micronaut.context.event.ApplicationEvent;
import io.micronaut.inject.ExecutableMethod;

public class CircuitClosedEvent extends ApplicationEvent {
   public CircuitClosedEvent(ExecutableMethod<?, ?> source) {
      super(source);
   }

   public ExecutableMethod<?, ?> getSource() {
      return (ExecutableMethod<?, ?>)super.getSource();
   }
}
