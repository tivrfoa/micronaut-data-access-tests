package io.micronaut.data.event;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanProperty;

public interface EntityEventContext<T> extends PersistenceEventContext<T> {
   @NonNull
   T getEntity();

   <P> void setProperty(BeanProperty<T, P> property, P newValue);

   default boolean supportsEventSystem() {
      return true;
   }
}
