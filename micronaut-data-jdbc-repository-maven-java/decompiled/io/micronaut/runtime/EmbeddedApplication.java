package io.micronaut.runtime;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextLifeCycle;
import io.micronaut.context.env.Environment;

public interface EmbeddedApplication<T extends EmbeddedApplication> extends ApplicationContextLifeCycle<T> {
   @Override
   ApplicationContext getApplicationContext();

   ApplicationConfiguration getApplicationConfiguration();

   default Environment getEnvironment() {
      return this.getApplicationContext().getEnvironment();
   }

   default boolean isServer() {
      return false;
   }

   default boolean isForceExit() {
      return false;
   }
}
