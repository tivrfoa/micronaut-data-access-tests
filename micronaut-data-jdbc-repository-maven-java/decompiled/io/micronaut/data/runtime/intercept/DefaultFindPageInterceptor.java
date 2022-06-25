package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.FindPageInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.List;

public class DefaultFindPageInterceptor<T, R> extends AbstractQueryInterceptor<T, R> implements FindPageInterceptor<T, R> {
   protected DefaultFindPageInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   @Override
   public R intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context) {
      Class<R> returnType = context.getReturnType().getType();
      if (context.hasAnnotation(Query.class)) {
         PreparedQuery<?, ?> preparedQuery = this.prepareQuery(methodKey, context);
         PreparedQuery<?, Number> countQuery = this.prepareCountQuery(methodKey, context);
         Iterable<?> iterable = this.operations.findAll(preparedQuery);
         List<R> resultList = CollectionUtils.iterableToList(iterable);
         Number n = this.operations.findOne(countQuery);
         Long result = n != null ? n.longValue() : 0L;
         Page<R> page = Page.of(resultList, this.getPageable(context), result);
         return (R)(returnType.isInstance(page)
            ? page
            : this.operations
               .getConversionService()
               .convert(page, returnType)
               .orElseThrow(() -> new IllegalStateException("Unsupported page interface type " + returnType)));
      } else {
         Page page = this.operations.findPage(this.getPagedQuery(context));
         return (R)(returnType.isInstance(page)
            ? page
            : this.operations
               .getConversionService()
               .convert(page, returnType)
               .orElseThrow(() -> new IllegalStateException("Unsupported page interface type " + returnType)));
      }
   }
}
