package io.micronaut.data.event;

import io.micronaut.data.model.runtime.RuntimePersistentEntity;

public interface PersistenceEventContext<T> {
   RuntimePersistentEntity<T> getPersistentEntity();
}
