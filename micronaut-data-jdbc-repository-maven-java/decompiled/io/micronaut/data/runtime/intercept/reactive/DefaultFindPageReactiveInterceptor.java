package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.FindPageReactiveInterceptor;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DefaultFindPageReactiveInterceptor extends AbstractReactiveInterceptor<Object, Object> implements FindPageReactiveInterceptor<Object, Object> {
   protected DefaultFindPageReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      Publisher<Page<Object>> publisher;
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<?, ?> preparedQuery = this.prepareQuery(methodKey, context);
         PreparedQuery<?, Number> countQuery = this.prepareCountQuery(methodKey, context);
         TransactionSynchronizationManager.TransactionSynchronizationState state = TransactionSynchronizationManager.getState();
         publisher = Flux.from(this.reactiveOperations.findOne(countQuery)).flatMap(total -> (Mono)TransactionSynchronizationManager.withState(state, () -> {
               Flux<Object> resultList = Flux.from(this.reactiveOperations.findAll(preparedQuery));
               return resultList.collectList().map(list -> Page.of(list, preparedQuery.getPageable(), total.longValue()));
            }));
      } else {
         publisher = this.reactiveOperations.findPage(this.getPagedQuery(context));
      }

      return Publishers.convertPublisher(publisher, context.getReturnType().getType());
   }
}
