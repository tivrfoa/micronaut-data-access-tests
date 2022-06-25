package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.async.FindPageAsyncInterceptor;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class DefaultFindPageAsyncInterceptor<T> extends AbstractAsyncInterceptor<T, Page<Object>> implements FindPageAsyncInterceptor<T> {
   protected DefaultFindPageAsyncInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public CompletionStage<Page<Object>> intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, CompletionStage<Page<Object>>> context) {
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<?, ?> preparedQuery = this.prepareQuery(methodKey, context);
         PreparedQuery<?, Number> countQuery = this.prepareCountQuery(methodKey, context);
         TransactionSynchronizationManager.TransactionSynchronizationState state = TransactionSynchronizationManager.getState();
         return this.asyncDatastoreOperations
            .findOne(countQuery)
            .thenCompose(
               total -> TransactionSynchronizationManager.withState(state, () -> this.asyncDatastoreOperations.findAll(preparedQuery).thenApply(objects -> {
                        List<Object> resultList = CollectionUtils.iterableToList(objects);
                        return Page.of(resultList, this.getPageable(context), total.longValue());
                     }))
            );
      } else {
         return this.asyncDatastoreOperations.findPage(this.getPagedQuery(context));
      }
   }
}
