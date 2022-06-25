package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.ReturnType;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.SaveOneInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Map;

public class DefaultSaveOneInterceptor<T> extends AbstractQueryInterceptor<T, Object> implements SaveOneInterceptor<T> {
   protected DefaultSaveOneInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, Object> context) {
      Class<?> rootEntity = this.getRequiredRootEntity(context);
      Map<String, Object> valueMap = this.getParameterValueMap(context);
      Object instance = this.instantiateEntity(rootEntity, valueMap);
      instance = this.operations.persist(this.getInsertOperation(context, instance));
      ReturnType<Object> rt = context.getReturnType();
      return this.isNumber(rt.getType())
         ? this.operations
            .getConversionService()
            .convert(1, rt.asArgument())
            .orElseThrow(() -> new IllegalStateException("Unsupported return type: " + rt.getType()))
         : instance;
   }
}
