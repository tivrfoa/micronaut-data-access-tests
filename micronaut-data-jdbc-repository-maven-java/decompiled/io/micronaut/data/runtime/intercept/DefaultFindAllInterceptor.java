package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.FindAllInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Collections;

public class DefaultFindAllInterceptor<T, R> extends AbstractQueryInterceptor<T, Iterable<R>> implements FindAllInterceptor<T, R> {
   protected DefaultFindAllInterceptor(RepositoryOperations datastore) {
      super(datastore);
   }

   public Iterable<R> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, Iterable<R>> context) {
      Class<Iterable<R>> rt = context.getReturnType().getType();
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<?, ?> preparedQuery = this.prepareQuery(methodKey, context);
         Iterable<?> iterable = this.operations.findAll(preparedQuery);
         return rt.isInstance(iterable)
            ? iterable
            : (Iterable)this.operations.getConversionService().convert(iterable, context.getReturnType().asArgument()).orElse(Collections.emptyList());
      } else {
         PagedQuery<R> pagedQuery = this.getPagedQuery(context);
         Iterable<R> iterable = this.operations.findAll(pagedQuery);
         return rt.isInstance(iterable)
            ? iterable
            : (Iterable)this.operations.getConversionService().convert(iterable, context.getReturnType().asArgument()).orElse(Collections.emptyList());
      }
   }
}
