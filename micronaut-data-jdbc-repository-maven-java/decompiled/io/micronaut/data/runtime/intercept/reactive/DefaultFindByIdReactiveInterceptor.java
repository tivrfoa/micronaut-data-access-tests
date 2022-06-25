package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.FindByIdReactiveInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import java.io.Serializable;
import org.reactivestreams.Publisher;

public class DefaultFindByIdReactiveInterceptor extends AbstractReactiveInterceptor<Object, Object> implements FindByIdReactiveInterceptor<Object, Object> {
   protected DefaultFindByIdReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      Class<?> rootEntity = this.getRequiredRootEntity(context);
      Object id = context.getParameterValues()[0];
      if (!(id instanceof Serializable)) {
         throw new IllegalArgumentException("Entity IDs must be serializable!");
      } else {
         Publisher<Object> publisher = this.reactiveOperations.findOne(rootEntity, (Serializable)id);
         return Publishers.convertPublisher(publisher, context.getReturnType().getType());
      }
   }
}
