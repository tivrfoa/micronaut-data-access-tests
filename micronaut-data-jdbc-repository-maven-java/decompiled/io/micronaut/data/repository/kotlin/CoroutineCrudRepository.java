package io.micronaut.data.repository.kotlin;

import io.micronaut.data.repository.GenericRepository;
import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
   mv = {1, 6, 0},
   k = 1,
   xi = 48,
   d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u001c\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\bf\u0018\u0000*\u0004\b\u0000\u0010\u0001*\u0004\b\u0001\u0010\u00022\u000e\u0012\u0004\u0012\u0002H\u0001\u0012\u0004\u0012\u0002H\u00020\u0003J\u0011\u0010\u0004\u001a\u00020\u0005H¦@ø\u0001\u0000¢\u0006\u0002\u0010\u0006J\u0019\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00028\u0000H¦@ø\u0001\u0000¢\u0006\u0002\u0010\nJ\u0011\u0010\u000b\u001a\u00020\bH¦@ø\u0001\u0000¢\u0006\u0002\u0010\u0006J\u001f\u0010\u000b\u001a\u00020\b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00028\u00000\rH¦@ø\u0001\u0000¢\u0006\u0002\u0010\u000eJ\u0019\u0010\u000f\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00028\u0001H¦@ø\u0001\u0000¢\u0006\u0002\u0010\nJ\u0019\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0010\u001a\u00028\u0001H¦@ø\u0001\u0000¢\u0006\u0002\u0010\nJ\u000e\u0010\u0013\u001a\b\u0012\u0004\u0012\u00028\u00000\u0014H&J\u001b\u0010\u0015\u001a\u0004\u0018\u00018\u00002\u0006\u0010\u0010\u001a\u00028\u0001H¦@ø\u0001\u0000¢\u0006\u0002\u0010\nJ#\u0010\u0016\u001a\u0002H\u0017\"\b\b\u0002\u0010\u0017*\u00028\u00002\u0006\u0010\t\u001a\u0002H\u0017H¦@ø\u0001\u0000¢\u0006\u0002\u0010\nJ&\u0010\u0018\u001a\b\u0012\u0004\u0012\u0002H\u00170\u0014\"\b\b\u0002\u0010\u0017*\u00028\u00002\f\u0010\f\u001a\b\u0012\u0004\u0012\u0002H\u00170\rH&J#\u0010\u0019\u001a\u0002H\u0017\"\b\b\u0002\u0010\u0017*\u00028\u00002\u0006\u0010\t\u001a\u0002H\u0017H¦@ø\u0001\u0000¢\u0006\u0002\u0010\nJ&\u0010\u001a\u001a\b\u0012\u0004\u0012\u0002H\u00170\u0014\"\b\b\u0002\u0010\u0017*\u00028\u00002\f\u0010\f\u001a\b\u0012\u0004\u0012\u0002H\u00170\rH&\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u001b"},
   d2 = {"Lio/micronaut/data/repository/kotlin/CoroutineCrudRepository;", "E", "ID", "Lio/micronaut/data/repository/GenericRepository;", "count", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "", "entity", "(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAll", "entities", "", "(Ljava/lang/Iterable;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteById", "id", "existsById", "", "findAll", "Lkotlinx/coroutines/flow/Flow;", "findById", "save", "S", "saveAll", "update", "updateAll", "data-model"}
)
public interface CoroutineCrudRepository<E, ID> extends GenericRepository<E, ID> {
   @Nullable
   <S extends E> Object save(S var1, @NotNull Continuation<? super S> var2);

   @Nullable
   <S extends E> Object update(S var1, @NotNull Continuation<? super S> var2);

   @NotNull
   <S extends E> Flow<S> updateAll(@NotNull Iterable<? extends S> var1);

   @NotNull
   <S extends E> Flow<S> saveAll(@NotNull Iterable<? extends S> var1);

   @Nullable
   Object findById(ID var1, @NotNull Continuation<? super E> var2);

   @Nullable
   Object existsById(ID var1, @NotNull Continuation<? super Boolean> var2);

   @NotNull
   Flow<E> findAll();

   @Nullable
   Object count(@NotNull Continuation<? super Long> var1);

   @Nullable
   Object deleteById(ID var1, @NotNull Continuation<? super Integer> var2);

   @Nullable
   Object delete(E var1, @NotNull Continuation<? super Integer> var2);

   @Nullable
   Object deleteAll(@NotNull Iterable<? extends E> var1, @NotNull Continuation<? super Integer> var2);

   @Nullable
   Object deleteAll(@NotNull Continuation<? super Integer> var1);
}
