package io.micronaut.inject;

import io.micronaut.core.type.Executable;
import java.lang.reflect.Method;

public interface MethodInjectionPoint<B, T> extends CallableInjectionPoint<B>, Executable<B, T> {
   Method getMethod();

   String getName();

   boolean isPreDestroyMethod();

   boolean isPostConstructMethod();

   @Override
   T invoke(B instance, Object... args);

   @Override
   default Class<B> getDeclaringType() {
      return this.getDeclaringBean().getBeanType();
   }
}
