package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.FindByIdAsyncInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import java.io.Serializable;
import java.util.concurrent.CompletionStage;

public class DefaultFindByIdAsyncInterceptor<T> extends AbstractAsyncInterceptor<T, Object> implements FindByIdAsyncInterceptor<T> {
   protected DefaultFindByIdAsyncInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Object> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Object>> context) {
      Class<?> rootEntity = this.getRequiredRootEntity(context);
      Object id = context.getParameterValues()[0];
      if (!(id instanceof Serializable)) {
         throw new IllegalArgumentException("Entity IDs must be serializable!");
      } else {
         return this.asyncDatastoreOperations.findOne(rootEntity, (Serializable)id).thenApply(o -> this.convertOne(context, o));
      }
   }
}
