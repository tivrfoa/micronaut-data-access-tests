package io.micronaut.data.runtime.intercept.criteria.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.ReturnType;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.intercept.criteria.AbstractSpecificationInterceptor;
import org.reactivestreams.Publisher;

@Internal
public class UpdateAllReactiveSpecificationInterceptor extends AbstractReactiveSpecificationInterceptor<Object, Object> {
   protected UpdateAllReactiveSpecificationInterceptor(RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      PreparedQuery<?, Number> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.UPDATE_ALL);
      ReturnType<Object> returnType = context.getReturnType();
      Publisher<Number> publisher = this.reactiveOperations.executeUpdate(preparedQuery);
      return Publishers.convertPublisher(publisher, returnType.getType());
   }
}
