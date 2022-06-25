package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.FindSliceAsyncInterceptor;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Slice;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.concurrent.CompletionStage;

public class DefaultFindSliceAsyncInterceptor<T> extends AbstractAsyncInterceptor<T, Slice<Object>> implements FindSliceAsyncInterceptor<T> {
   protected DefaultFindSliceAsyncInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Slice<Object>> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Slice<Object>>> context) {
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<?, ?> preparedQuery = this.prepareQuery(methodKey, context);
         Pageable pageable = preparedQuery.getPageable();
         return this.asyncDatastoreOperations.findAll(preparedQuery).thenApply(objects -> Slice.of(CollectionUtils.iterableToList(objects), pageable));
      } else {
         PagedQuery<Object> pagedQuery = this.getPagedQuery(context);
         return this.asyncDatastoreOperations
            .findAll(pagedQuery)
            .thenApply(objects -> Slice.of(CollectionUtils.iterableToList(objects), pagedQuery.getPageable()));
      }
   }
}
