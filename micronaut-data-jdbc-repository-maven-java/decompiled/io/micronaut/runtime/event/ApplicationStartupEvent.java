package io.micronaut.runtime.event;

import io.micronaut.runtime.EmbeddedApplication;

public class ApplicationStartupEvent extends AbstractEmbeddedApplicationEvent {
   public ApplicationStartupEvent(EmbeddedApplication<?> source) {
      super(source);
   }
}
