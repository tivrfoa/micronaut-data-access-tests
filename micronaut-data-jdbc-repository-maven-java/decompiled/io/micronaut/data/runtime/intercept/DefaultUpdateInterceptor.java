package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.UpdateInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;

public class DefaultUpdateInterceptor<T> extends AbstractQueryInterceptor<T, Object> implements UpdateInterceptor<T> {
   public DefaultUpdateInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, Object> context) {
      PreparedQuery<?, Number> preparedQuery = this.prepareQuery(methodKey, context);
      Number number = (Number)this.operations.executeUpdate(preparedQuery).orElse(null);
      Argument<Object> returnType = context.getReturnType().asArgument();
      Class<Object> type = ReflectionUtils.getWrapperType(returnType.getType());
      if (Number.class.isAssignableFrom(type)) {
         return type.isInstance(number) ? number : this.operations.getConversionService().convert(number, returnType).orElse(0);
      } else {
         return !Boolean.class.isAssignableFrom(type) ? null : number == null || number.longValue() < 0L;
      }
   }
}
