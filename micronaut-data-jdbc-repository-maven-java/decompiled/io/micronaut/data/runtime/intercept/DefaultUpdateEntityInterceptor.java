package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.ReturnType;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.UpdateEntityInterceptor;
import io.micronaut.data.operations.RepositoryOperations;

public class DefaultUpdateEntityInterceptor<T> extends AbstractQueryInterceptor<T, Object> implements UpdateEntityInterceptor<T> {
   protected DefaultUpdateEntityInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, Object> context) {
      Object entity = this.getEntityParameter(context, Object.class);
      entity = this.operations.update(this.getUpdateOperation(context, entity));
      ReturnType<Object> rt = context.getReturnType();
      return this.isNumber(rt.getType())
         ? this.operations
            .getConversionService()
            .convert(1, rt.asArgument())
            .orElseThrow(() -> new IllegalStateException("Unsupported return type: " + rt.getType()))
         : entity;
   }
}
