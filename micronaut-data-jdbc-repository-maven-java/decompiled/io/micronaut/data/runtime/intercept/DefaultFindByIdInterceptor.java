package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.FindByIdInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.operations.RepositoryOperations;
import java.io.Serializable;

public class DefaultFindByIdInterceptor<T> extends AbstractQueryInterceptor<T, Object> implements FindByIdInterceptor<T> {
   public DefaultFindByIdInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, Object> context) {
      Class<?> rootEntity = this.getRequiredRootEntity(context);
      Object id = context.getParameterValues()[0];
      if (!(id instanceof Serializable)) {
         throw new IllegalArgumentException("Entity IDs must be serializable!");
      } else {
         return this.convertOne(context, this.operations.findOne(rootEntity, (Serializable)id));
      }
   }
}
