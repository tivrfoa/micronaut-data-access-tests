package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.FindOneReactiveInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import org.reactivestreams.Publisher;

public class DefaultFindOneReactiveInterceptor extends AbstractReactiveInterceptor<Object, Object> implements FindOneReactiveInterceptor<Object, Object> {
   protected DefaultFindOneReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      PreparedQuery<Object, Object> preparedQuery = this.prepareQuery(methodKey, context);
      Publisher<Object> publisher = this.reactiveOperations.findOptional(preparedQuery);
      return Publishers.convertPublisher(publisher, context.getReturnType().getType());
   }
}
