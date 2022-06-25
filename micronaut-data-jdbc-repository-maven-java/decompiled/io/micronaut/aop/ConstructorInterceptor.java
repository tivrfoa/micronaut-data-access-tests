package io.micronaut.aop;

import io.micronaut.core.annotation.NonNull;

@FunctionalInterface
public interface ConstructorInterceptor<T> extends Interceptor<T, T> {
   @NonNull
   T intercept(@NonNull ConstructorInvocationContext<T> context);

   @NonNull
   @Override
   default T intercept(@NonNull InvocationContext<T, T> context) {
      if (context instanceof ConstructorInvocationContext) {
         return this.intercept((ConstructorInvocationContext<T>)context);
      } else {
         throw new IllegalArgumentException("Context must be an instance of MethodInvocationContext");
      }
   }
}
