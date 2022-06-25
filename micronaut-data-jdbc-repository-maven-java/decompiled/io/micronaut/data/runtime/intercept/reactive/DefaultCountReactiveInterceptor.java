package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.CountReactiveInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import org.reactivestreams.Publisher;

public class DefaultCountReactiveInterceptor extends AbstractReactiveInterceptor<Object, Object> implements CountReactiveInterceptor<Object, Object> {
   protected DefaultCountReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<?, Long> preparedQuery = this.prepareQuery(methodKey, context, Long.class);
         return Publishers.convertPublisher(this.reactiveOperations.findAll(preparedQuery), context.getReturnType().getType());
      } else {
         Publisher<Long> result = this.reactiveOperations.count(this.getPagedQuery(context));
         return Publishers.convertPublisher(result, context.getReturnType().getType());
      }
   }
}
