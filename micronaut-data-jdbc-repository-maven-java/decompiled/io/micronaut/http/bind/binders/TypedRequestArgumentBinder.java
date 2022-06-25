package io.micronaut.http.bind.binders;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.bind.TypeArgumentBinder;
import io.micronaut.http.HttpRequest;
import java.util.Collections;
import java.util.List;

public interface TypedRequestArgumentBinder<T> extends RequestArgumentBinder<T>, TypeArgumentBinder<T, HttpRequest<?>> {
   @NonNull
   default List<Class<?>> superTypes() {
      return Collections.emptyList();
   }
}
