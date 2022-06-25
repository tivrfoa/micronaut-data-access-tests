package io.micronaut.aop.chain;

import io.micronaut.aop.Adapter;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.ExecutionHandle;
import io.micronaut.inject.qualifiers.Qualifiers;

@Internal
final class AdapterIntroduction implements MethodInterceptor<Object, Object> {
   private final ExecutionHandle<?, ?> executionHandle;

   AdapterIntroduction(BeanContext beanContext, ExecutableMethod<?, ?> method) {
      Class<?> beanType = (Class)method.classValue(Adapter.class, "adaptedBean").orElse(null);
      if (beanType == null) {
         throw new IllegalStateException("No bean type to adapt found in Adapter configuration for method: " + method);
      } else {
         String beanMethod = (String)method.stringValue(Adapter.class, "adaptedMethod").orElse(null);
         if (StringUtils.isEmpty(beanMethod)) {
            throw new IllegalStateException("No bean method to adapt found in Adapter configuration for method: " + method);
         } else {
            String beanQualifier = (String)method.stringValue(Adapter.class, "adaptedQualifier").orElse(null);
            Class[] argumentTypes = method.classValues(Adapter.class, "adaptedArgumentTypes");
            Class[] methodArgumentTypes = method.getArgumentTypes();
            if (StringUtils.isNotEmpty(beanQualifier)) {
               this.executionHandle = (ExecutionHandle)beanContext.findExecutionHandle(
                     beanType,
                     Qualifiers.byName(beanQualifier),
                     beanMethod,
                     argumentTypes.length == methodArgumentTypes.length ? argumentTypes : methodArgumentTypes
                  )
                  .orElseThrow(
                     () -> new IllegalStateException("Cannot adapt method [" + method + "]. Target method [" + beanMethod + "] not found on bean " + beanType)
                  );
            } else {
               this.executionHandle = (ExecutionHandle)beanContext.findExecutionHandle(
                     beanType, beanMethod, argumentTypes.length == methodArgumentTypes.length ? argumentTypes : methodArgumentTypes
                  )
                  .orElseThrow(
                     () -> new IllegalStateException("Cannot adapt method [" + method + "]. Target method [" + beanMethod + "] not found on bean " + beanType)
                  );
            }

         }
      }
   }

   @Nullable
   @Override
   public Object intercept(MethodInvocationContext<Object, Object> context) {
      return this.executionHandle.invoke(context.getParameterValues());
   }
}
