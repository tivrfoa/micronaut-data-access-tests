package io.micronaut.data.repository.jpa.kotlin;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.jpa.criteria.DeleteSpecification;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import io.micronaut.data.repository.jpa.criteria.QuerySpecification;
import io.micronaut.data.repository.jpa.criteria.UpdateSpecification;
import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
   mv = {1, 6, 0},
   k = 1,
   xi = 48,
   d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u0000*\u0004\b\u0000\u0010\u00012\u00020\u0002J!\u0010\u0003\u001a\u00020\u00042\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\u0006H¦@ø\u0001\u0000¢\u0006\u0002\u0010\u0007J!\u0010\u0003\u001a\u00020\u00042\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\bH¦@ø\u0001\u0000¢\u0006\u0002\u0010\tJ!\u0010\n\u001a\u00020\u00042\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\u000bH¦@ø\u0001\u0000¢\u0006\u0002\u0010\fJ!\u0010\n\u001a\u00020\u00042\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\u0006H¦@ø\u0001\u0000¢\u0006\u0002\u0010\u0007J\u001e\u0010\r\u001a\b\u0012\u0004\u0012\u00028\u00000\u000e2\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\u0006H&J&\u0010\r\u001a\b\u0012\u0004\u0012\u00028\u00000\u000f2\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\u00062\u0006\u0010\u0010\u001a\u00020\u0011H&J&\u0010\r\u001a\b\u0012\u0004\u0012\u00028\u00000\u000e2\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\u00062\u0006\u0010\u0012\u001a\u00020\u0013H&J\u001e\u0010\r\u001a\b\u0012\u0004\u0012\u00028\u00000\u000e2\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\bH&J&\u0010\r\u001a\b\u0012\u0004\u0012\u00028\u00000\u000f2\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\b2\u0006\u0010\u0010\u001a\u00020\u0011H&J&\u0010\r\u001a\b\u0012\u0004\u0012\u00028\u00000\u000e2\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\b2\u0006\u0010\u0012\u001a\u00020\u0013H&J\u001f\u0010\u0014\u001a\u0004\u0018\u00018\u00002\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\u0006H&¢\u0006\u0002\u0010\u0015J\u001f\u0010\u0014\u001a\u0004\u0018\u00018\u00002\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\bH&¢\u0006\u0002\u0010\u0016J!\u0010\u0017\u001a\u00020\u00042\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\u0018H¦@ø\u0001\u0000¢\u0006\u0002\u0010\u0019\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u001a"},
   d2 = {"Lio/micronaut/data/repository/jpa/kotlin/CoroutineJpaSpecificationExecutor;", "T", "", "count", "", "spec", "Lio/micronaut/data/repository/jpa/criteria/PredicateSpecification;", "(Lio/micronaut/data/repository/jpa/criteria/PredicateSpecification;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Lio/micronaut/data/repository/jpa/criteria/QuerySpecification;", "(Lio/micronaut/data/repository/jpa/criteria/QuerySpecification;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAll", "Lio/micronaut/data/repository/jpa/criteria/DeleteSpecification;", "(Lio/micronaut/data/repository/jpa/criteria/DeleteSpecification;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "findAll", "Lkotlinx/coroutines/flow/Flow;", "Lio/micronaut/data/model/Page;", "pageable", "Lio/micronaut/data/model/Pageable;", "sort", "Lio/micronaut/data/model/Sort;", "findOne", "(Lio/micronaut/data/repository/jpa/criteria/PredicateSpecification;)Ljava/lang/Object;", "(Lio/micronaut/data/repository/jpa/criteria/QuerySpecification;)Ljava/lang/Object;", "updateAll", "Lio/micronaut/data/repository/jpa/criteria/UpdateSpecification;", "(Lio/micronaut/data/repository/jpa/criteria/UpdateSpecification;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data-model"}
)
public interface CoroutineJpaSpecificationExecutor<T> {
   @Nullable
   T findOne(@Nullable QuerySpecification<T> var1);

   @Nullable
   T findOne(@Nullable PredicateSpecification<T> var1);

   @NotNull
   Flow<T> findAll(@Nullable QuerySpecification<T> var1);

   @NotNull
   Flow<T> findAll(@Nullable PredicateSpecification<T> var1);

   @NotNull
   Page<T> findAll(@Nullable QuerySpecification<T> var1, @NotNull Pageable var2);

   @NotNull
   Page<T> findAll(@Nullable PredicateSpecification<T> var1, @NotNull Pageable var2);

   @NotNull
   Flow<T> findAll(@Nullable QuerySpecification<T> var1, @NotNull Sort var2);

   @NotNull
   Flow<T> findAll(@Nullable PredicateSpecification<T> var1, @NotNull Sort var2);

   @Nullable
   Object count(@Nullable QuerySpecification<T> var1, @NotNull Continuation<? super Long> var2);

   @Nullable
   Object count(@Nullable PredicateSpecification<T> var1, @NotNull Continuation<? super Long> var2);

   @Nullable
   Object deleteAll(@Nullable DeleteSpecification<T> var1, @NotNull Continuation<? super Long> var2);

   @Nullable
   Object deleteAll(@Nullable PredicateSpecification<T> var1, @NotNull Continuation<? super Long> var2);

   @Nullable
   Object updateAll(@Nullable UpdateSpecification<T> var1, @NotNull Continuation<? super Long> var2);
}
