package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.FindStreamInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.stream.Stream;

public class DefaultFindStreamInterceptor<T> extends AbstractQueryInterceptor<T, Stream<T>> implements FindStreamInterceptor<T> {
   public DefaultFindStreamInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public Stream<T> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, Stream<T>> context) {
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<?, ?> preparedQuery = this.prepareQuery(methodKey, context);
         return this.operations.findStream(preparedQuery);
      } else {
         return this.operations.findStream(this.getPagedQuery(context));
      }
   }
}
