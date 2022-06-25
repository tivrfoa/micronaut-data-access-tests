package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.ReturnType;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.UpdateAllEntitiesInterceptor;
import io.micronaut.data.operations.RepositoryOperations;

public class DefaultUpdateAllEntitiesInterceptor<T, R> extends AbstractQueryInterceptor<T, R> implements UpdateAllEntitiesInterceptor<T, R> {
   public DefaultUpdateAllEntitiesInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public R intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context) {
      Iterable<R> iterable = this.getEntitiesParameter(context, Object.class);
      Class<R> rootEntity = this.getRequiredRootEntity(context);
      Iterable<R> rs = this.operations.updateAll(this.getUpdateAllBatchOperation(context, rootEntity, iterable));
      ReturnType<R> rt = context.getReturnType();
      if (rt.isVoid()) {
         return null;
      } else {
         return (R)(this.isNumber(rt.getType())
            ? this.operations
               .getConversionService()
               .convert(this.count(rs), rt.asArgument())
               .orElseThrow(() -> new IllegalStateException("Unsupported return type: " + rt.getType()))
            : this.operations
               .getConversionService()
               .convert(rs, rt.asArgument())
               .orElseThrow(() -> new IllegalStateException("Unsupported iterable return type: " + rt.getType())));
      }
   }
}
