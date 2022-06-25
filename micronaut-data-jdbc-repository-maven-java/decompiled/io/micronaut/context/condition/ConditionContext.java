package io.micronaut.context.condition;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.value.PropertyResolver;
import java.util.Collections;
import java.util.List;

public interface ConditionContext<T extends AnnotationMetadataProvider> extends BeanLocator, PropertyResolver {
   T getComponent();

   BeanContext getBeanContext();

   BeanResolutionContext getBeanResolutionContext();

   ConditionContext<T> fail(@NonNull Failure failure);

   default ConditionContext<T> fail(@NonNull String failure) {
      return this.fail(Failure.simple(failure));
   }

   default List<Failure> getFailures() {
      return Collections.emptyList();
   }

   default boolean isFailing() {
      return !this.getFailures().isEmpty();
   }
}
