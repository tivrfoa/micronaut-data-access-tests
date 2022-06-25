package io.micronaut.aop;

import io.micronaut.core.annotation.Nullable;

public interface MethodInterceptor<T, R> extends Interceptor<T, R> {
   @Nullable
   R intercept(MethodInvocationContext<T, R> context);

   @Nullable
   @Override
   default R intercept(InvocationContext<T, R> context) {
      if (context instanceof MethodInvocationContext) {
         return this.intercept((MethodInvocationContext<T, R>)context);
      } else {
         throw new IllegalArgumentException("Context must be an instance of MethodInvocationContext");
      }
   }
}
