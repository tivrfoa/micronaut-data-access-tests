package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import java.util.Collections;
import java.util.Map;

public interface PreparedQuery<E, R> extends PagedQuery<E>, StoredQuery<E, R>, PreparedDataOperation<R> {
   Class<?> getRepositoryType();

   @NonNull
   @Deprecated
   Map<String, Object> getParameterValues();

   Object[] getParameterArray();

   Argument[] getArguments();

   @Deprecated
   default Class<?> getLastUpdatedType() {
      throw new IllegalStateException("Not supported anymore");
   }

   @NonNull
   @Override
   default Map<String, Object> getQueryHints() {
      return Collections.emptyMap();
   }
}
