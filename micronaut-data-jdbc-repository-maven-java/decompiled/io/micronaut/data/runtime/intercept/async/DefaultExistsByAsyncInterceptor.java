package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.ExistsByAsyncInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.concurrent.CompletionStage;

public class DefaultExistsByAsyncInterceptor<T> extends AbstractAsyncInterceptor<T, Boolean> implements ExistsByAsyncInterceptor<T> {
   protected DefaultExistsByAsyncInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Boolean> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Boolean>> context) {
      PreparedQuery<?, Boolean> preparedQuery = this.prepareQuery(methodKey, context, null);
      return this.asyncDatastoreOperations.exists(preparedQuery);
   }
}
