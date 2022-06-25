package io.micronaut.data.operations.reactive;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.operations.RepositoryOperations;

public interface ReactiveCapableRepository extends RepositoryOperations {
   @NonNull
   ReactiveRepositoryOperations reactive();
}
