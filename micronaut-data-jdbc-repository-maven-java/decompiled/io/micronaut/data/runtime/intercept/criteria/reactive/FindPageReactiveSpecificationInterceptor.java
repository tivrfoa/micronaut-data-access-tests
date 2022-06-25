package io.micronaut.data.runtime.intercept.criteria.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.intercept.criteria.AbstractSpecificationInterceptor;
import java.util.List;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Internal
public class FindPageReactiveSpecificationInterceptor extends AbstractReactiveSpecificationInterceptor<Object, Object> {
   protected FindPageReactiveSpecificationInterceptor(RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      if (context.getParameterValues().length != 2) {
         throw new IllegalStateException("Expected exactly 2 arguments to method");
      } else {
         Pageable pageable = this.getPageable(context);
         Publisher<?> result;
         if (pageable.isUnpaged()) {
            PreparedQuery<?, ?> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.FIND_PAGE);
            Flux<?> results = Flux.from(this.reactiveOperations.findAll(preparedQuery));
            result = results.collectList().map(resultList -> Page.of(resultList, pageable, (long)resultList.size()));
         } else {
            PreparedQuery<?, ?> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.FIND_PAGE);
            PreparedQuery<?, Number> countQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.COUNT);
            Flux<?> results = Flux.from(this.reactiveOperations.findAll(preparedQuery));
            Mono<Number> count = Mono.from(this.reactiveOperations.findOne(countQuery));
            result = results.collectList()
               .zipWith(count)
               .map(tuple -> Page.of((List)tuple.getT1(), this.getPageable(context), ((Number)tuple.getT2()).longValue()));
         }

         return Publishers.convertPublisher(result, context.getReturnType().getType());
      }
   }
}
