package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.FindSliceReactiveInterceptor;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Slice;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DefaultFindSliceReactiveInterceptor extends AbstractReactiveInterceptor<Object, Object> implements FindSliceReactiveInterceptor<Object, Object> {
   protected DefaultFindSliceReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<Object, Object> preparedQuery = this.prepareQuery(methodKey, context);
         Pageable pageable = preparedQuery.getPageable();
         Mono<Slice<Object>> publisher = Flux.from(this.reactiveOperations.findAll(preparedQuery)).collectList().map(objects -> Slice.of(objects, pageable));
         return Publishers.convertPublisher(publisher, context.getReturnType().getType());
      } else {
         PagedQuery<Object> pagedQuery = this.getPagedQuery(context);
         Mono<? extends Slice<?>> result = Flux.from(this.reactiveOperations.findAll(pagedQuery))
            .collectList()
            .map(objects -> Slice.of(objects, pagedQuery.getPageable()));
         return Publishers.convertPublisher(result, context.getReturnType().getType());
      }
   }
}
