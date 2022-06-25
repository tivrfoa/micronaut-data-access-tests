package io.micronaut.data.runtime.intercept.criteria;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.ReturnType;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;

@Internal
public class DeleteAllSpecificationInterceptor extends AbstractSpecificationInterceptor<Object, Object> {
   protected DeleteAllSpecificationInterceptor(RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      PreparedQuery<?, Number> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.DELETE_ALL);
      ReturnType<Object> rt = context.getReturnType();
      Number result = (Number)this.operations.executeDelete(preparedQuery).orElse(0);
      if (rt.isVoid()) {
         return null;
      } else {
         return rt.getType().isInstance(result) ? result : this.operations.getConversionService().convertRequired(result, rt.asArgument());
      }
   }
}
