package io.micronaut.inject;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanConstructor;

public interface ConstructorInjectionPoint<T> extends CallableInjectionPoint<T>, BeanConstructor<T> {
   T invoke(Object... args);

   @NonNull
   @Override
   default Class<T> getDeclaringBeanType() {
      return this.getDeclaringBean().getBeanType();
   }

   @NonNull
   @Override
   default T instantiate(Object... parameterValues) {
      return this.invoke(parameterValues);
   }
}
