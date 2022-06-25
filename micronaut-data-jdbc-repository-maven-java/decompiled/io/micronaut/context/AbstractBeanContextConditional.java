package io.micronaut.context;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.Failure;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.BeanConfiguration;
import io.micronaut.inject.BeanContextConditional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
abstract class AbstractBeanContextConditional implements BeanContextConditional, AnnotationMetadataProvider {
   static final Logger LOG = LoggerFactory.getLogger(Condition.class);

   @Override
   public boolean isEnabled(@NonNull BeanContext context, @Nullable BeanResolutionContext resolutionContext) {
      AnnotationMetadata annotationMetadata = this.getAnnotationMetadata();
      Condition condition = annotationMetadata.hasStereotype(Requires.class) ? new RequiresCondition(annotationMetadata) : null;
      DefaultConditionContext<AbstractBeanContextConditional> conditionContext = new DefaultConditionContext<>(
         (DefaultBeanContext)context, this, resolutionContext
      );
      boolean enabled = condition == null || condition.matches(conditionContext);
      if (LOG.isDebugEnabled() && !enabled) {
         if (this instanceof BeanConfiguration) {
            LOG.debug(this + " will not be loaded due to failing conditions:");
         } else {
            LOG.debug("Bean [" + this + "] will not be loaded due to failing conditions:");
         }

         for(Failure failure : conditionContext.getFailures()) {
            LOG.debug("* {}", failure.getMessage());
         }
      }

      return enabled;
   }
}
