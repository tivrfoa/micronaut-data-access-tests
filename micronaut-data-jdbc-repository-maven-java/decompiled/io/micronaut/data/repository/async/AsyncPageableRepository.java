package io.micronaut.data.repository.async;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import java.util.concurrent.CompletableFuture;

public interface AsyncPageableRepository<E, ID> extends AsyncCrudRepository<E, ID> {
   @NonNull
   CompletableFuture<E> findAll(@NonNull Sort sort);

   @NonNull
   CompletableFuture<Page<E>> findAll(@NonNull Pageable pageable);
}
