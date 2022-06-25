package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.FindOptionalInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Optional;

public class DefaultFindOptionalInterceptor<T> extends AbstractQueryInterceptor<T, Optional<Object>> implements FindOptionalInterceptor<T> {
   public DefaultFindOptionalInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public Optional<Object> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, Optional<Object>> context) {
      PreparedQuery<?, ?> preparedQuery = this.prepareQuery(methodKey, context);
      Object result = this.operations.findOne(preparedQuery);
      return Optional.ofNullable(result);
   }
}
