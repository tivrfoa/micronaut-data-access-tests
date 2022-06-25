package io.micronaut.runtime.event;

import io.micronaut.context.event.ApplicationEvent;
import io.micronaut.runtime.EmbeddedApplication;

public abstract class AbstractEmbeddedApplicationEvent extends ApplicationEvent {
   public AbstractEmbeddedApplicationEvent(EmbeddedApplication<?> embeddedApplication) {
      super(embeddedApplication);
   }

   public EmbeddedApplication<?> getSource() {
      return (EmbeddedApplication<?>)super.getSource();
   }
}
