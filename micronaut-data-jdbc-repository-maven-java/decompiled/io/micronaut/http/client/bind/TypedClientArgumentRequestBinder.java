package io.micronaut.http.client.bind;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import java.util.Collections;
import java.util.List;

public interface TypedClientArgumentRequestBinder<T> extends ClientArgumentRequestBinder<T> {
   @NonNull
   Argument<T> argumentType();

   @NonNull
   default List<Class<?>> superTypes() {
      return Collections.emptyList();
   }
}
