package io.micronaut.data.runtime.intercept.criteria.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.intercept.criteria.AbstractSpecificationInterceptor;
import org.reactivestreams.Publisher;

@Internal
public class CountReactiveSpecificationInterceptor extends AbstractReactiveSpecificationInterceptor<Object, Publisher<Number>> {
   public CountReactiveSpecificationInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   public Publisher<Number> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Publisher<Number>> context) {
      PreparedQuery<?, Long> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.COUNT);
      return Publishers.convertPublisher(this.reactiveOperations.findAll(preparedQuery), context.getReturnType().getType());
   }
}
