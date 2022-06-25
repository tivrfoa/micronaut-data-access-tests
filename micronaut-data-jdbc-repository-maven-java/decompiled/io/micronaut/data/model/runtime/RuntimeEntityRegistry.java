package io.micronaut.data.model.runtime;

import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.event.EntityEventListener;

public interface RuntimeEntityRegistry extends ApplicationContextProvider {
   @NonNull
   EntityEventListener<Object> getEntityEventListener();

   @NonNull
   Object autoPopulateRuntimeProperty(@NonNull RuntimePersistentProperty<?> persistentProperty, @Nullable Object previousValue);

   @NonNull
   <T> RuntimePersistentEntity<T> getEntity(@NonNull Class<T> type);

   @NonNull
   <T> RuntimePersistentEntity<T> newEntity(@NonNull Class<T> type);
}
