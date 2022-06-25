package io.micronaut.data.repository.reactive;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.repository.GenericRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public interface RxJavaCrudRepository<E, ID> extends GenericRepository<E, ID> {
   @NonNull
   <S extends E> Single<S> save(@Valid @NotNull @NonNull S entity);

   @NonNull
   <S extends E> Flowable<S> saveAll(@Valid @NotNull @NonNull Iterable<S> entities);

   @NonNull
   Maybe<E> findById(@NotNull @NonNull ID id);

   @NonNull
   <S extends E> Single<S> update(@Valid @NotNull @NonNull S entity);

   @NonNull
   <S extends E> Flowable<S> updateAll(@Valid @NotNull @NonNull Iterable<S> entities);

   @NonNull
   Single<Boolean> existsById(@NotNull @NonNull ID id);

   @NonNull
   Flowable<E> findAll();

   @NonNull
   Single<Long> count();

   @NonNull
   Completable deleteById(@NonNull @NotNull ID id);

   @NonNull
   Completable delete(@NonNull @NotNull E entity);

   @NonNull
   Completable deleteAll(@NonNull @NotNull Iterable<? extends E> entities);

   @NonNull
   Completable deleteAll();
}
