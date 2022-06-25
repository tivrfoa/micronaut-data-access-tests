package io.micronaut.data.repository.kotlin;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
   mv = {1, 6, 0},
   k = 1,
   xi = 48,
   d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u0000*\u0004\b\u0000\u0010\u0001*\u0004\b\u0001\u0010\u00022\u000e\u0012\u0004\u0012\u0002H\u0001\u0012\u0004\u0012\u0002H\u00020\u0003J\u001f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00028\u00000\u00052\u0006\u0010\u0006\u001a\u00020\u0007H¦@ø\u0001\u0000¢\u0006\u0002\u0010\bJ\u001f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00028\u00000\t2\u0006\u0010\n\u001a\u00020\u000bH¦@ø\u0001\u0000¢\u0006\u0002\u0010\f\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\r"},
   d2 = {"Lio/micronaut/data/repository/kotlin/CoroutinePageableCrudRepository;", "E", "ID", "Lio/micronaut/data/repository/kotlin/CoroutineCrudRepository;", "findAll", "Lio/micronaut/data/model/Page;", "pageable", "Lio/micronaut/data/model/Pageable;", "(Lio/micronaut/data/model/Pageable;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Lkotlinx/coroutines/flow/Flow;", "sort", "Lio/micronaut/data/model/Sort;", "(Lio/micronaut/data/model/Sort;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data-model"}
)
public interface CoroutinePageableCrudRepository<E, ID> extends CoroutineCrudRepository<E, ID> {
   @Nullable
   Object findAll(@NotNull Sort var1, @NotNull Continuation<? super Flow<? extends E>> var2);

   @Nullable
   Object findAll(@NotNull Pageable var1, @NotNull Continuation<? super Page<E>> var2);
}
