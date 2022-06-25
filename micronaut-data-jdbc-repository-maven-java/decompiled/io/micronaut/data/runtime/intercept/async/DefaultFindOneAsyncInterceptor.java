package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.FindOneAsyncInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.concurrent.CompletionStage;

public class DefaultFindOneAsyncInterceptor<T> extends AbstractAsyncInterceptor<T, Object> implements FindOneAsyncInterceptor<T> {
   protected DefaultFindOneAsyncInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Object> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Object>> context) {
      PreparedQuery<Object, Object> preparedQuery = this.prepareQuery(methodKey, context);
      return this.asyncDatastoreOperations.findOne(preparedQuery).thenApply(o -> this.convertOne(context, o));
   }
}
