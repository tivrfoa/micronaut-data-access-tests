package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.SaveAllAsyncInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.concurrent.CompletionStage;

public class DefaultSaveAllAsyncInterceptor<T> extends AbstractAsyncInterceptor<T, Object> implements SaveAllAsyncInterceptor<T> {
   protected DefaultSaveAllAsyncInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Object> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Object>> context) {
      Iterable<Object> iterable = this.getEntitiesParameter(context, Object.class);
      CompletionStage<Iterable<Object>> cs = this.asyncDatastoreOperations.persistAll(this.getInsertBatchOperation(context, iterable));
      Argument<?> csValueArgument = this.getReturnType(context);
      return this.isNumber(csValueArgument.getType())
         ? cs.thenApply(it -> this.operations.getConversionService().convertRequired(this.count(it), csValueArgument))
         : cs;
   }
}
