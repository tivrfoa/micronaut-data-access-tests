package io.micronaut.aop;

import io.micronaut.inject.proxy.InterceptedBeanProxy;
import io.micronaut.inject.qualifiers.Qualified;

public interface InterceptedProxy<T> extends Intercepted, Qualified<T>, InterceptedBeanProxy<T> {
   @Override
   T interceptedTarget();

   @Override
   default boolean hasCachedInterceptedTarget() {
      return false;
   }
}
