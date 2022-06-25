package io.micronaut.inject;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanResolutionContext;
import io.micronaut.core.annotation.Internal;

@Internal
public interface DisposableBeanDefinition<T> extends BeanDefinition<T> {
   default T dispose(BeanContext context, T bean) {
      Object var5;
      try (DefaultBeanResolutionContext rc = new DefaultBeanResolutionContext(context, this)) {
         var5 = this.dispose(rc, context, bean);
      }

      return (T)var5;
   }

   T dispose(BeanResolutionContext resolutionContext, BeanContext context, T bean);
}
