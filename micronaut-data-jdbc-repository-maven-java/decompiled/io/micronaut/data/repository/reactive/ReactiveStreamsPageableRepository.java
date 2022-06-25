package io.micronaut.data.repository.reactive;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import org.reactivestreams.Publisher;

public interface ReactiveStreamsPageableRepository<E, ID> extends ReactiveStreamsCrudRepository<E, ID> {
   @NonNull
   Publisher<E> findAll(@NonNull Sort sort);

   @NonNull
   Publisher<Page<E>> findAll(@NonNull Pageable pageable);
}
