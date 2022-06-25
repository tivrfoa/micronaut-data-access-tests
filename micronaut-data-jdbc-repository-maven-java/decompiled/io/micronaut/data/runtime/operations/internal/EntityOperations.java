package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;

@Internal
public abstract class EntityOperations<T, Exc extends Exception> extends BaseOperations<T, Exc> {
   public EntityOperations(EntityEventListener<Object> entityEventListener, RuntimePersistentEntity<T> persistentEntity, ConversionService<?> conversionService) {
      super(entityEventListener, persistentEntity, conversionService);
   }
}
