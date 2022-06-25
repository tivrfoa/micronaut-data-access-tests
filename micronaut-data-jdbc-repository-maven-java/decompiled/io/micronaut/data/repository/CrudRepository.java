package io.micronaut.data.repository;

import io.micronaut.core.annotation.Blocking;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.validation.Validated;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Blocking
@Validated
public interface CrudRepository<E, ID> extends GenericRepository<E, ID> {
   @NonNull
   <S extends E> S save(@Valid @NotNull @NonNull S entity);

   @NonNull
   <S extends E> S update(@Valid @NotNull @NonNull S entity);

   @NonNull
   <S extends E> Iterable<S> updateAll(@Valid @NotNull @NonNull Iterable<S> entities);

   @NonNull
   <S extends E> Iterable<S> saveAll(@Valid @NotNull @NonNull Iterable<S> entities);

   @NonNull
   Optional<E> findById(@NotNull @NonNull ID id);

   boolean existsById(@NotNull @NonNull ID id);

   @NonNull
   Iterable<E> findAll();

   long count();

   void deleteById(@NonNull @NotNull ID id);

   void delete(@NonNull @NotNull E entity);

   void deleteAll(@NonNull @NotNull Iterable<? extends E> entities);

   void deleteAll();
}
