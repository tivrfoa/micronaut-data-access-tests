package io.micronaut.core.beans;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import java.util.Map;

public interface BeanMap<T> extends Map<String, Object> {
   @NonNull
   Class<T> getBeanType();

   @NonNull
   static <B> BeanMap<B> of(@NonNull B bean) {
      ArgumentUtils.requireNonNull("bean", bean);
      BeanIntrospection<B> introspection = BeanIntrospector.SHARED.getIntrospection(bean.getClass());
      return new BeanIntrospectionMap<>(introspection, bean);
   }
}
