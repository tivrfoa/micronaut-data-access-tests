package io.micronaut.context.event;

import java.util.EventObject;

public class ApplicationEvent extends EventObject {
   public ApplicationEvent(Object source) {
      super(source);
   }
}
