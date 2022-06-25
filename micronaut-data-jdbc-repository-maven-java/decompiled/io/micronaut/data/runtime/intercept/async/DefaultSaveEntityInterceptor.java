package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.SaveEntityAsyncInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.concurrent.CompletionStage;

public class DefaultSaveEntityInterceptor<T> extends AbstractAsyncInterceptor<T, Object> implements SaveEntityAsyncInterceptor<T> {
   protected DefaultSaveEntityInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Object> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Object>> context) {
      Object entity = this.getEntityParameter(context, Object.class);
      CompletionStage<Object> cs = this.asyncDatastoreOperations.persist(this.getInsertOperation(context, entity));
      Argument<?> csValueArgument = this.getReturnType(context);
      return this.isNumber(csValueArgument.getType())
         ? cs.thenApply(it -> this.operations.getConversionService().convertRequired(it == null ? 0 : 1, csValueArgument))
         : cs;
   }
}
