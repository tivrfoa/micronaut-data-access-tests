package io.micronaut.aop;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Executable;
import io.micronaut.inject.ExecutableMethod;

public interface MethodInvocationContext<T, R> extends InvocationContext<T, R>, Executable<T, R>, ExecutableMethod<T, R> {
   @NonNull
   ExecutableMethod<T, R> getExecutableMethod();

   @Override
   default boolean isSuspend() {
      return this.getExecutableMethod().isSuspend();
   }

   @Override
   default boolean isAbstract() {
      return this.getExecutableMethod().isAbstract();
   }

   @Override
   default Class<T> getDeclaringType() {
      return this.getExecutableMethod().getDeclaringType();
   }
}
