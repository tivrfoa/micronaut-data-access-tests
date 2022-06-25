package io.micronaut.inject;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

@FunctionalInterface
public interface BeanContextConditional {
   default boolean isEnabled(@NonNull BeanContext context) {
      return this.isEnabled(context, null);
   }

   boolean isEnabled(@NonNull BeanContext context, @Nullable BeanResolutionContext resolutionContext);
}
