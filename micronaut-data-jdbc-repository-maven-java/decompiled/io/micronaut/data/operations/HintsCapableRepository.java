package io.micronaut.data.operations;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.runtime.StoredQuery;
import java.util.Collections;
import java.util.Map;

public interface HintsCapableRepository {
   @NonNull
   default Map<String, Object> getQueryHints(@NonNull StoredQuery<?, ?> storedQuery) {
      return Collections.emptyMap();
   }
}
