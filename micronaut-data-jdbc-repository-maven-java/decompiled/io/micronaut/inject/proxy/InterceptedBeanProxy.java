package io.micronaut.inject.proxy;

import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.qualifiers.Qualified;

@Internal
public interface InterceptedBeanProxy<T> extends InterceptedBean, Qualified<T> {
   T interceptedTarget();

   default boolean hasCachedInterceptedTarget() {
      return false;
   }
}
