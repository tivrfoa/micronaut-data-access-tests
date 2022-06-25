package io.micronaut.data.operations.reactive;

import io.micronaut.core.annotation.NonNull;

public interface ReactorReactiveCapableRepository extends ReactiveCapableRepository {
   @NonNull
   ReactorReactiveRepositoryOperations reactive();
}
