package io.micronaut.data.runtime.intercept.criteria.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.intercept.criteria.AbstractSpecificationInterceptor;

@Internal
public class DeleteAllAsyncSpecificationInterceptor extends AbstractAsyncSpecificationInterceptor<Object, Object> {
   protected DeleteAllAsyncSpecificationInterceptor(RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      PreparedQuery<?, Number> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.DELETE_ALL);
      return this.asyncOperations.executeDelete(preparedQuery).thenApply(number -> this.convertNumberToReturnType(context, number));
   }
}
