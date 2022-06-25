package io.micronaut.data.runtime.intercept.criteria.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.intercept.criteria.AbstractSpecificationInterceptor;
import java.util.Collections;
import java.util.concurrent.CompletionStage;

@Internal
public class FindAllAsyncSpecificationInterceptor extends AbstractAsyncSpecificationInterceptor<Object, Object> {
   protected FindAllAsyncSpecificationInterceptor(RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      PreparedQuery<?, ?> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.FIND_ALL);
      CompletionStage<? extends Iterable<?>> future = this.asyncOperations.findAll(preparedQuery);
      return future.thenApply(iterable -> {
         Argument<?> argument = this.findReturnType(context, LIST_OF_OBJECTS);
         if (argument.getType().isInstance(iterable)) {
            return iterable;
         } else {
            Iterable<Object> result = (Iterable)this.operations.getConversionService().convert(iterable, argument).orElse(null);
            return (Iterable)(result == null ? Collections.emptyList() : result);
         }
      });
   }
}
