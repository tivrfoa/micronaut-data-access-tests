package io.micronaut.aop;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanConstructor;

public interface ConstructorInvocationContext<T> extends InvocationContext<T, T> {
   @NonNull
   BeanConstructor<T> getConstructor();

   @Override
   default Class<T> getDeclaringType() {
      return this.getConstructor().getDeclaringBeanType();
   }

   @NonNull
   @Override
   T proceed() throws RuntimeException;

   @NonNull
   @Override
   T proceed(Interceptor from) throws RuntimeException;
}
