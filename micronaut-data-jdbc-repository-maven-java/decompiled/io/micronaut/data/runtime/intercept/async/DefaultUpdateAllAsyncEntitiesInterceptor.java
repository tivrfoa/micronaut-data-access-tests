package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.UpdateAllEntriesAsyncInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.concurrent.CompletionStage;

public class DefaultUpdateAllAsyncEntitiesInterceptor<T>
   extends AbstractAsyncInterceptor<T, Object>
   implements UpdateAllEntriesAsyncInterceptor<T, CompletionStage<Object>> {
   public DefaultUpdateAllAsyncEntitiesInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   public CompletionStage<Object> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Object>> context) {
      Iterable<T> iterable = this.getEntitiesParameter(context, Object.class);
      Class<T> rootEntity = this.getRequiredRootEntity(context);
      CompletionStage<Iterable<T>> future = this.asyncDatastoreOperations.updateAll(this.getUpdateAllBatchOperation(context, rootEntity, iterable));
      Argument<?> csValueArgument = this.findReturnType(context, LIST_OF_OBJECTS);
      return this.isNumber(csValueArgument.getType())
         ? future.thenApply(it -> this.convertNumberToReturnType(context, Integer.valueOf(this.count(it))))
         : future.thenApply(it -> this.operations.getConversionService().convertRequired(it, csValueArgument));
   }
}
