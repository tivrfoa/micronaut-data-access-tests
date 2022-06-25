package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.Named;

public interface EntityOperation<E> extends Named, PreparedDataOperation<E> {
   @NonNull
   Class<E> getRootEntity();

   @NonNull
   Class<?> getRepositoryType();

   @Nullable
   StoredQuery<E, ?> getStoredQuery();
}
