package io.micronaut.data.runtime.intercept.criteria;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.List;

@Internal
public class FindPageSpecificationInterceptor extends AbstractSpecificationInterceptor<Object, Object> {
   protected FindPageSpecificationInterceptor(RepositoryOperations operations) {
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
            Iterable<?> iterable = this.operations.findAll(preparedQuery);
            List<Object> resultList = CollectionUtils.iterableToList(iterable);
            return Page.of(resultList, pageable, (long)resultList.size());
         } else {
            PreparedQuery<?, ?> preparedQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.FIND_PAGE);
            PreparedQuery<?, Number> countQuery = this.preparedQueryForCriteria(methodKey, context, AbstractSpecificationInterceptor.Type.COUNT);
            Iterable<?> iterable = this.operations.findAll(preparedQuery);
            List<Object> resultList = CollectionUtils.iterableToList(iterable);
            Number count = this.operations.findOne(countQuery);
            Page page = Page.of(resultList, this.getPageable(context), count != null ? count.longValue() : 0L);
            Class<Object> rt = context.getReturnType().getType();
            return rt.isInstance(page)
               ? page
               : this.operations.getConversionService().convert(page, rt).orElseThrow(() -> new IllegalStateException("Unsupported page interface type " + rt));
         }
      }
   }
}
