package io.micronaut.context;

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.DefaultArgument;
import io.micronaut.inject.annotation.AbstractEnvironmentAnnotationMetadata;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;

@Internal
class EnvironmentAwareArgument<T> extends DefaultArgument<T> implements EnvironmentConfigurable {
   private final AnnotationMetadata annotationMetadata;
   private Environment environment;

   EnvironmentAwareArgument(DefaultArgument<T> argument) {
      super(argument.getType(), argument.getName(), argument.getAnnotationMetadata(), argument.getTypeVariables(), argument.getTypeParameters());
      this.annotationMetadata = this.initAnnotationMetadata(argument.getAnnotationMetadata());
   }

   @Override
   public boolean hasPropertyExpressions() {
      return this.annotationMetadata.hasPropertyExpressions();
   }

   @Override
   public void configure(Environment environment) {
      this.environment = environment;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   private AnnotationMetadata initAnnotationMetadata(@Nullable AnnotationMetadata annotationMetadata) {
      if (annotationMetadata instanceof DefaultAnnotationMetadata && annotationMetadata.hasPropertyExpressions()) {
         return new EnvironmentAwareArgument.ArgumentAnnotationMetadata((DefaultAnnotationMetadata)annotationMetadata);
      } else {
         return annotationMetadata != null ? annotationMetadata : AnnotationMetadata.EMPTY_METADATA;
      }
   }

   private final class ArgumentAnnotationMetadata extends AbstractEnvironmentAnnotationMetadata {
      ArgumentAnnotationMetadata(DefaultAnnotationMetadata targetMetadata) {
         super(targetMetadata);
      }

      @Nullable
      @Override
      protected Environment getEnvironment() {
         return EnvironmentAwareArgument.this.environment;
      }
   }
}
