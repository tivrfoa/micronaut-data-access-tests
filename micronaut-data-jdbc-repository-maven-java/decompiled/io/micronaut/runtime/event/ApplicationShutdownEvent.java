package io.micronaut.runtime.event;

import io.micronaut.runtime.EmbeddedApplication;

public class ApplicationShutdownEvent extends AbstractEmbeddedApplicationEvent {
   public ApplicationShutdownEvent(EmbeddedApplication<?> source) {
      super(source);
   }
}
