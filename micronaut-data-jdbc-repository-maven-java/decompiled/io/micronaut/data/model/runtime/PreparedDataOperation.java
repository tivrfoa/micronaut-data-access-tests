package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.attr.AttributeHolder;
import java.util.Optional;

public interface PreparedDataOperation<R> extends StoredDataOperation<R>, AttributeHolder {
   default <RT> Optional<RT> getParameterInRole(@NonNull String role, @NonNull Class<RT> type) {
      return Optional.empty();
   }
}
