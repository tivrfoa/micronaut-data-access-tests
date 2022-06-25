package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.DeleteOneInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Optional;

public class DefaultDeleteOneInterceptor<T> extends AbstractQueryInterceptor<T, Object> implements DeleteOneInterceptor<T> {
   protected DefaultDeleteOneInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, Object> context) {
      Class<Object> returnType = context.getReturnType().getType();
      Optional<Object> deleteEntity = this.findEntityParameter(context, Object.class);
      if (deleteEntity.isPresent()) {
         Object entity = deleteEntity.get();
         Class<?> rootEntity = this.getRequiredRootEntity(context);
         if (!rootEntity.isInstance(entity)) {
            throw new IllegalArgumentException("Entity argument must be an instance of " + rootEntity.getName());
         } else {
            Number deleted = this.operations.delete(this.getDeleteOperation(context, entity));
            if (this.isNumber(returnType)) {
               return this.operations.getConversionService().convertRequired(deleted, returnType);
            } else if (returnType.equals(rootEntity)) {
               return deleted.intValue() > 0 ? entity : null;
            } else {
               return null;
            }
         }
      } else {
         throw new IllegalStateException("Argument not found");
      }
   }
}
