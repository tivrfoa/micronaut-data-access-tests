package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.FindSliceInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Slice;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;

public class DefaultFindSliceInterceptor<T, R> extends AbstractQueryInterceptor<T, R> implements FindSliceInterceptor<T, R> {
   protected DefaultFindSliceInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   @Override
   public R intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context) {
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<?, ?> preparedQuery = this.prepareQuery(methodKey, context);
         Pageable pageable = preparedQuery.getPageable();
         Iterable<R> iterable = this.operations.findAll(preparedQuery);
         Slice<R> slice = Slice.of(CollectionUtils.iterableToList(iterable), pageable);
         return this.convertOrFail(context, slice);
      } else {
         PagedQuery<Object> pagedQuery = this.getPagedQuery(context);
         Iterable iterable = this.operations.findAll(pagedQuery);
         Slice<R> slice = Slice.of(CollectionUtils.iterableToList(iterable), pagedQuery.getPageable());
         return this.convertOrFail(context, slice);
      }
   }

   private R convertOrFail(MethodInvocationContext<T, R> context, Slice<R> slice) {
      ReturnType<R> returnType = context.getReturnType();
      return (R)(returnType.getType().isInstance(slice)
         ? slice
         : this.operations
            .getConversionService()
            .convert(slice, returnType.asArgument())
            .orElseThrow(() -> new IllegalStateException("Unsupported slice interface: " + returnType.getType())));
   }
}
