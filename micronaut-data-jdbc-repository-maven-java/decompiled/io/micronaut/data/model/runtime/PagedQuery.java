package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Named;
import io.micronaut.data.model.Pageable;
import java.util.Collections;
import java.util.Map;

public interface PagedQuery<E> extends Named, AnnotationMetadataProvider {
   @NonNull
   Class<E> getRootEntity();

   @NonNull
   Pageable getPageable();

   @NonNull
   default Map<String, Object> getQueryHints() {
      return Collections.emptyMap();
   }
}
