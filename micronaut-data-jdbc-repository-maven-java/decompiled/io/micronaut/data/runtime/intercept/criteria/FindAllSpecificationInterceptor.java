package io.micronaut.data.runtime.intercept.criteria;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Collections;

@Internal
public class FindAllSpecificationInterceptor extends AbstractSpecificationInterceptor<Object, Object> {
   protected FindAllSpecificationInterceptor(RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      PreparedQuery<?, ?> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.FIND_ALL);
      Class<Object> rt = context.getReturnType().getType();
      Iterable<?> iterable = this.operations.findAll(preparedQuery);
      return rt.isInstance(iterable)
         ? iterable
         : this.operations.getConversionService().convert(iterable, context.getReturnType().asArgument()).orElse(Collections.emptyList());
   }
}
