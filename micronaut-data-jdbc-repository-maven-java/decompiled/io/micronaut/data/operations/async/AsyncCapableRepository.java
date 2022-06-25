package io.micronaut.data.operations.async;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.operations.RepositoryOperations;

public interface AsyncCapableRepository extends RepositoryOperations {
   @NonNull
   AsyncRepositoryOperations async();
}
