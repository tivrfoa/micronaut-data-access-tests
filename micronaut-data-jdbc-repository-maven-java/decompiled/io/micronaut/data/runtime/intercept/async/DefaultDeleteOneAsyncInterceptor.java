package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.DeleteOneAsyncInterceptor;
import io.micronaut.data.model.runtime.DeleteOperation;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.concurrent.CompletionStage;

public class DefaultDeleteOneAsyncInterceptor<T> extends AbstractAsyncInterceptor<T, Object> implements DeleteOneAsyncInterceptor<T, Object> {
   protected DefaultDeleteOneAsyncInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Object> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Object>> context) {
      Argument<CompletionStage<Object>> arg = context.getReturnType().asArgument();
      Object entity = this.getEntityParameter(context, Object.class);
      if (entity != null) {
         DeleteOperation<Object> deleteOperation = this.getDeleteOperation(context, entity);
         return this.asyncDatastoreOperations.delete(deleteOperation).thenApply(number -> this.convertNumberToReturnType(context, number));
      } else {
         throw new IllegalArgumentException("Entity to delete cannot be null");
      }
   }
}
