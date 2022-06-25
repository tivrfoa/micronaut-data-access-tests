package io.micronaut.data.repository.reactive;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactorPageableRepository<E, ID> extends ReactorCrudRepository<E, ID>, ReactiveStreamsPageableRepository<E, ID> {
   @NonNull
   Flux<E> findAll(@NonNull Sort sort);

   @NonNull
   Mono<Page<E>> findAll(@NonNull Pageable pageable);
}
