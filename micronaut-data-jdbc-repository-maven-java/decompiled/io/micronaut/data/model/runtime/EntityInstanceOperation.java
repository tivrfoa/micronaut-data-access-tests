package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.NonNull;

public interface EntityInstanceOperation<E> extends EntityOperation<E> {
   @NonNull
   E getEntity();
}
