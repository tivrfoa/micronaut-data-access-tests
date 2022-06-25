package io.micronaut.data.runtime.intercept.criteria.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.intercept.criteria.AbstractSpecificationInterceptor;
import org.reactivestreams.Publisher;

@Internal
public class FindOneReactiveSpecificationInterceptor extends AbstractReactiveSpecificationInterceptor<Object, Object> {
   protected FindOneReactiveSpecificationInterceptor(RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      PreparedQuery<Object, Object> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.FIND_ONE);
      Publisher<Object> publisher = this.reactiveOperations.findOptional(preparedQuery);
      return Publishers.convertPublisher(publisher, context.getReturnType().getType());
   }
}
