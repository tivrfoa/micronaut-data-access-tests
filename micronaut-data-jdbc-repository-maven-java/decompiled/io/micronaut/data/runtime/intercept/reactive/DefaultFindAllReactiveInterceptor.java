package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.FindAllReactiveInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import org.reactivestreams.Publisher;

public class DefaultFindAllReactiveInterceptor extends AbstractReactiveInterceptor<Object, Object> implements FindAllReactiveInterceptor<Object, Object> {
   protected DefaultFindAllReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      Publisher<?> publisher;
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<?, ?> preparedQuery = this.prepareQuery(methodKey, context);
         publisher = this.reactiveOperations.findAll(preparedQuery);
      } else {
         publisher = this.reactiveOperations.findAll(this.getPagedQuery(context));
      }

      return Publishers.convertPublisher(publisher, context.getReturnType().getType());
   }
}
