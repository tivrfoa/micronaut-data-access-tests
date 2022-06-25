package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.ReturnType;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.UpdateReactiveInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import org.reactivestreams.Publisher;

public class DefaultUpdateReactiveInterceptor extends AbstractReactiveInterceptor<Object, Object> implements UpdateReactiveInterceptor<Object, Object> {
   protected DefaultUpdateReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      PreparedQuery<?, Number> preparedQuery = this.prepareQuery(methodKey, context);
      ReturnType<Object> returnType = context.getReturnType();
      Publisher<Number> publisher = this.reactiveOperations.executeUpdate(preparedQuery);
      return Publishers.convertPublisher(publisher, returnType.getType());
   }
}
