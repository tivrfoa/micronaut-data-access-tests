package io.micronaut.data.repository.reactive;

import io.micronaut.core.annotation.NonNull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactorCrudRepository<E, ID> extends ReactiveStreamsCrudRepository<E, ID> {
   @NonNull
   <S extends E> Mono<S> save(@NonNull @Valid @NotNull S entity);

   @NonNull
   <S extends E> Flux<S> saveAll(@NonNull @Valid @NotNull Iterable<S> entities);

   @NonNull
   <S extends E> Mono<S> update(@NonNull @Valid @NotNull S entity);

   @NonNull
   <S extends E> Flux<S> updateAll(@NonNull @Valid @NotNull Iterable<S> entities);

   @NonNull
   Mono<E> findById(@NonNull @NotNull ID id);

   @NonNull
   Mono<Boolean> existsById(@NonNull @NotNull ID id);

   @NonNull
   Flux<E> findAll();

   @NonNull
   Mono<Long> count();

   @NonNull
   Mono<Long> deleteById(@NonNull @NotNull ID id);

   @NonNull
   Mono<Long> delete(@NonNull @NotNull E entity);

   @NonNull
   Mono<Long> deleteAll(@NonNull @NotNull Iterable<? extends E> entities);

   @NonNull
   Mono<Long> deleteAll();
}
