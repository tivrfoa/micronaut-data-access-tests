package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.CountAsyncInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Iterator;
import java.util.concurrent.CompletionStage;

public class DefaultCountAsyncInterceptor<T> extends AbstractAsyncInterceptor<T, Long> implements CountAsyncInterceptor<T> {
   protected DefaultCountAsyncInterceptor(RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Long> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Long>> context) {
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<?, Long> preparedQuery = this.prepareQuery(methodKey, context, Long.class);
         return this.asyncDatastoreOperations.findAll(preparedQuery).thenApply(longs -> {
            long result = 0L;
            Iterator<Long> i = longs.iterator();
            if (i.hasNext()) {
               result = i.next();
            }

            return result;
         });
      } else {
         return this.asyncDatastoreOperations.count(this.getPagedQuery(context));
      }
   }
}
