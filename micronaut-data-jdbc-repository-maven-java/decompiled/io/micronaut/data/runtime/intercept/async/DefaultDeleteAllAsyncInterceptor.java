package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.DeleteAllAsyncInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class DefaultDeleteAllAsyncInterceptor<T> extends AbstractAsyncInterceptor<T, Object> implements DeleteAllAsyncInterceptor<T, Object> {
   protected DefaultDeleteAllAsyncInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Object> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Object>> context) {
      Optional<Iterable<Object>> deleteEntities = this.findEntitiesParameter(context, Object.class);
      Optional<Object> deleteEntity = this.findEntityParameter(context, Object.class);
      CompletionStage<Number> cs;
      if (!deleteEntity.isPresent() && !deleteEntities.isPresent()) {
         PreparedQuery<?, Number> preparedQuery = this.prepareQuery(methodKey, context);
         cs = this.asyncDatastoreOperations.executeDelete(preparedQuery);
      } else if (deleteEntity.isPresent()) {
         cs = this.asyncDatastoreOperations.delete(this.getDeleteOperation(context, deleteEntity.get()));
      } else {
         cs = this.asyncDatastoreOperations.deleteAll(this.getDeleteBatchOperation(context, (Iterable<T>)deleteEntities.get()));
      }

      return cs.thenApply(number -> this.convertNumberToReturnType(context, number));
   }
}
