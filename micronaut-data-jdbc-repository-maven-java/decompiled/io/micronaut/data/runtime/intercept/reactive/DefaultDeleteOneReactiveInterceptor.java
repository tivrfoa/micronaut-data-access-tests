package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.DeleteOneReactiveInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import org.reactivestreams.Publisher;

public class DefaultDeleteOneReactiveInterceptor extends AbstractReactiveInterceptor<Object, Object> implements DeleteOneReactiveInterceptor<Object, Object> {
   protected DefaultDeleteOneReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      Object entity = this.getEntityParameter(context, Object.class);
      if (entity != null) {
         Publisher<Number> publisher = this.reactiveOperations.delete(this.getDeleteOperation(context, entity));
         return Publishers.convertPublisher(publisher, context.getReturnType().getType());
      } else {
         throw new IllegalArgumentException("Entity to delete cannot be null");
      }
   }
}
