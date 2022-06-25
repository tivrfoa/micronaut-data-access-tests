package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.ExistsByReactiveInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;

public class DefaultExistsByReactiveInterceptor extends AbstractReactiveInterceptor<Object, Object> implements ExistsByReactiveInterceptor<Object, Object> {
   protected DefaultExistsByReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      PreparedQuery<?, Boolean> preparedQuery = this.prepareQuery(methodKey, context, null);
      return Publishers.convertPublisher(this.reactiveOperations.exists(preparedQuery), context.getReturnType().getType());
   }
}
