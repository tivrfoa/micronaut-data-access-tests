package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import java.util.List;

@Internal
public abstract class SyncEntitiesOperations<T, Exc extends Exception> extends EntitiesOperations<T, Exc> {
   public SyncEntitiesOperations(
      EntityEventListener<Object> entityEventListener, RuntimePersistentEntity<T> persistentEntity, ConversionService<?> conversionService
   ) {
      super(entityEventListener, persistentEntity, conversionService);
   }

   public abstract List<T> getEntities();
}
