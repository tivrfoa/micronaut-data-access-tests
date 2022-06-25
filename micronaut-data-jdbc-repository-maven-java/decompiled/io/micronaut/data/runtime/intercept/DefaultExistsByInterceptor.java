package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.ExistsByInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;

public class DefaultExistsByInterceptor<T> extends AbstractQueryInterceptor<T, Boolean> implements ExistsByInterceptor<T> {
   protected DefaultExistsByInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public Boolean intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, Boolean> context) {
      PreparedQuery<?, Boolean> preparedQuery = this.prepareQuery(methodKey, context, null);
      return this.operations.exists(preparedQuery);
   }
}
