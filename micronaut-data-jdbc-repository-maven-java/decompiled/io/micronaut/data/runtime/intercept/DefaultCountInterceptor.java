package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.CountInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Iterator;

public class DefaultCountInterceptor<T> extends AbstractQueryInterceptor<T, Number> implements CountInterceptor<T> {
   protected DefaultCountInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public Number intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, Number> context) {
      long result;
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<?, Long> preparedQuery = this.prepareQuery(methodKey, context, Long.class, true);
         Iterable<Long> iterable = this.operations.findAll(preparedQuery);
         Iterator<Long> i = iterable.iterator();
         result = i.hasNext() ? i.next() : 0L;
      } else {
         result = this.operations.count(this.getPagedQuery(context));
      }

      return (Number)this.operations
         .getConversionService()
         .convert(result, context.getReturnType().asArgument())
         .orElseThrow(() -> new IllegalStateException("Unsupported number type: " + context.getReturnType().getType()));
   }
}
