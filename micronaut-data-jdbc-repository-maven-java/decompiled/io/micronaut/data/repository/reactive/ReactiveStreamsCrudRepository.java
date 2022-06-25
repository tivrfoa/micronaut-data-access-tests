package io.micronaut.data.repository.reactive;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.repository.GenericRepository;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.reactivestreams.Publisher;

public interface ReactiveStreamsCrudRepository<E, ID> extends GenericRepository<E, ID> {
   @NonNull
   @SingleResult
   <S extends E> Publisher<S> save(@Valid @NotNull @NonNull S entity);

   @NonNull
   <S extends E> Publisher<S> saveAll(@Valid @NotNull @NonNull Iterable<S> entities);

   @NonNull
   <S extends E> Publisher<S> update(@Valid @NotNull @NonNull S entity);

   @NonNull
   <S extends E> Publisher<S> updateAll(@Valid @NotNull @NonNull Iterable<S> entities);

   @NonNull
   @SingleResult
   Publisher<E> findById(@NotNull @NonNull ID id);

   @SingleResult
   @NonNull
   Publisher<Boolean> existsById(@NotNull @NonNull ID id);

   @NonNull
   Publisher<E> findAll();

   @SingleResult
   @NonNull
   Publisher<Long> count();

   @NonNull
   @SingleResult
   Publisher<Long> deleteById(@NonNull @NotNull ID id);

   @SingleResult
   @NonNull
   Publisher<Long> delete(@NonNull @NotNull E entity);

   @SingleResult
   @NonNull
   Publisher<Long> deleteAll(@NonNull @NotNull Iterable<? extends E> entities);

   @NonNull
   Publisher<Long> deleteAll();
}
