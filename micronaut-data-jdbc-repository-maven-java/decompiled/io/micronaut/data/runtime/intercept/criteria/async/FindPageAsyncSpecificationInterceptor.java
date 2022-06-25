package io.micronaut.data.runtime.intercept.criteria.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.intercept.criteria.AbstractSpecificationInterceptor;
import java.util.List;

@Internal
public class FindPageAsyncSpecificationInterceptor extends AbstractAsyncSpecificationInterceptor<Object, Object> {
   protected FindPageAsyncSpecificationInterceptor(RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      if (context.getParameterValues().length != 2) {
         throw new IllegalStateException("Expected exactly 2 arguments to method");
      } else {
         Pageable pageable = this.getPageable(context);
         if (pageable.isUnpaged()) {
            PreparedQuery<?, ?> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.FIND_PAGE);
            return this.asyncOperations.findAll(preparedQuery).thenApply(iterable -> {
               List<?> resultList = CollectionUtils.iterableToList(iterable);
               return Page.of(resultList, pageable, (long)resultList.size());
            });
         } else {
            PreparedQuery<?, ?> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.FIND_PAGE);
            PreparedQuery<?, Number> countQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.COUNT);
            return this.asyncOperations
               .findAll(preparedQuery)
               .thenCompose(
                  iterable -> this.asyncOperations
                        .findOne(countQuery)
                        .thenApply(count -> Page.of(CollectionUtils.iterableToList(iterable), pageable, count.longValue()))
               );
         }
      }
   }
}
