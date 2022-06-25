package io.micronaut.data.runtime.intercept.criteria;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.ReturnType;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Iterator;

@Internal
public class CountSpecificationInterceptor extends AbstractSpecificationInterceptor<Object, Number> {
   public CountSpecificationInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   public Number intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Number> context) {
      PreparedQuery<?, Long> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.COUNT);
      Iterable<Long> iterable = this.operations.findAll(preparedQuery);
      Iterator<Long> i = iterable.iterator();
      Long result = i.hasNext() ? (Long)i.next() : 0L;
      ReturnType<Number> rt = context.getReturnType();
      Class<Number> returnType = rt.getType();
      return (Number)(returnType.isInstance(result) ? result : this.operations.getConversionService().convertRequired(result, rt.asArgument()));
   }
}
