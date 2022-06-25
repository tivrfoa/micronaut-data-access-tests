package io.micronaut.web.router;

import io.micronaut.context.BeanLocator;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.filter.HttpFilter;
import io.micronaut.inject.BeanDefinition;

@Internal
class BeanDefinitionFilterRoute extends DefaultFilterRoute {
   private final BeanDefinition<? extends HttpFilter> definition;

   BeanDefinitionFilterRoute(String pattern, BeanLocator beanLocator, BeanDefinition<? extends HttpFilter> definition) {
      super(pattern, () -> beanLocator.getBean(definition));
      this.definition = definition;
   }

   @NonNull
   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.definition.getAnnotationMetadata();
   }
}
