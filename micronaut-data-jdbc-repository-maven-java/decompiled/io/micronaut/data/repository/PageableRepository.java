package io.micronaut.data.repository;

import io.micronaut.core.annotation.Blocking;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;

@Blocking
public interface PageableRepository<E, ID> extends CrudRepository<E, ID> {
   @NonNull
   Iterable<E> findAll(@NonNull Sort sort);

   @NonNull
   Page<E> findAll(@NonNull Pageable pageable);
}
