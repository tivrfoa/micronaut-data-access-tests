package io.micronaut.data.runtime.intercept.criteria.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.intercept.criteria.AbstractSpecificationInterceptor;
import java.util.Iterator;
import java.util.concurrent.CompletionStage;

@Internal
public class CountAsyncSpecificationInterceptor extends AbstractAsyncSpecificationInterceptor<Object, CompletionStage<Number>> {
   public CountAsyncSpecificationInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   public CompletionStage<Number> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, CompletionStage<Number>> context) {
      PreparedQuery<?, Long> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.COUNT);
      return this.asyncOperations.findAll(preparedQuery).thenApply(longs -> {
         long result = 0L;
         Iterator<Long> i = longs.iterator();
         if (i.hasNext()) {
            result = i.next();
         }

         return result;
      });
   }
}
