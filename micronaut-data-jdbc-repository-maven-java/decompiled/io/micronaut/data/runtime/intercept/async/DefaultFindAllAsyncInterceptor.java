package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.FindAllAsyncInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Collections;
import java.util.concurrent.CompletionStage;

public class DefaultFindAllAsyncInterceptor<T> extends AbstractAsyncInterceptor<T, Iterable<Object>> implements FindAllAsyncInterceptor<T> {
   protected DefaultFindAllAsyncInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Iterable<Object>> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Iterable<Object>>> context) {
      CompletionStage<? extends Iterable<?>> future;
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<?, ?> preparedQuery = this.prepareQuery(methodKey, context);
         future = this.asyncDatastoreOperations.findAll(preparedQuery);
      } else {
         future = this.asyncDatastoreOperations.findAll(this.getPagedQuery(context));
      }

      return future.thenApply(iterable -> {
         Argument<?> argument = this.findReturnType(context, LIST_OF_OBJECTS);
         Iterable<Object> result = (Iterable)this.operations.getConversionService().convert(iterable, argument).orElse(null);
         return (Iterable)(result == null ? Collections.emptyList() : result);
      });
   }
}
