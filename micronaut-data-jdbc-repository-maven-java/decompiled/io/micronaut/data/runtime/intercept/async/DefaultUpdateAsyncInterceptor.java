package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.UpdateAsyncInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.concurrent.CompletionStage;

public class DefaultUpdateAsyncInterceptor<T> extends AbstractAsyncInterceptor<T, Number> implements UpdateAsyncInterceptor<T> {
   protected DefaultUpdateAsyncInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Number> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Number>> context) {
      PreparedQuery<?, Number> preparedQuery = this.prepareQuery(methodKey, context);
      return this.asyncDatastoreOperations.executeUpdate(preparedQuery).thenApply(n -> this.convertNumberToReturnType(context, n));
   }
}
