package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.FindOneInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;

public class DefaultFindOneInterceptor<T> extends AbstractQueryInterceptor<T, Object> implements FindOneInterceptor<T> {
   protected DefaultFindOneInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, Object> context) {
      PreparedQuery<?, ?> preparedQuery = this.prepareQuery(methodKey, context, null);
      return this.convertOne(context, this.operations.findOne(preparedQuery));
   }
}
