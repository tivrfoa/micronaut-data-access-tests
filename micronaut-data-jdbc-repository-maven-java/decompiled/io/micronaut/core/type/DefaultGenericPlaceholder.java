package io.micronaut.core.type;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;

@Internal
final class DefaultGenericPlaceholder<T> extends DefaultArgument<T> implements GenericPlaceholder<T> {
   private final String variableName;

   DefaultGenericPlaceholder(Class<T> type, String name, AnnotationMetadata annotationMetadata, Argument<?>... genericTypes) {
      super(type, name, annotationMetadata, genericTypes);
      this.variableName = name;
   }

   DefaultGenericPlaceholder(Class<T> type, String name, String variableName, AnnotationMetadata annotationMetadata, Argument<?>... genericTypes) {
      super(type, name, annotationMetadata, genericTypes);
      this.variableName = variableName;
   }

   @Override
   public String getVariableName() {
      return this.variableName;
   }

   @Override
   public boolean isTypeVariable() {
      return true;
   }
}
