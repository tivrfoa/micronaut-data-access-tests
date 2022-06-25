package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.SaveOneAsyncInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class DefaultSaveOneAsyncInterceptor<T> extends AbstractAsyncInterceptor<T, Object> implements SaveOneAsyncInterceptor<T> {
   protected DefaultSaveOneAsyncInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Object> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Object>> context) {
      Class<?> rootEntity = this.getRequiredRootEntity(context);
      Map<String, Object> parameterValueMap = this.getParameterValueMap(context);
      Object o = this.instantiateEntity(rootEntity, parameterValueMap);
      CompletionStage<Object> cs = this.asyncDatastoreOperations.persist(this.getInsertOperation(context, o));
      Argument<?> csValueArgument = this.getReturnType(context);
      return this.isNumber(csValueArgument.getType())
         ? cs.thenApply(it -> this.operations.getConversionService().convertRequired(it == null ? 0 : 1, csValueArgument))
         : cs;
   }
}
