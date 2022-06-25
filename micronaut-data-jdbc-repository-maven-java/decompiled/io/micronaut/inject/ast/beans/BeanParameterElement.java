package io.micronaut.inject.ast.beans;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.ast.ParameterElement;

public interface BeanParameterElement extends ParameterElement, InjectableElement {
   @Override
   default InjectableElement injectValue(String expression) {
      return InjectableElement.super.injectValue(expression);
   }

   @NonNull
   default BeanParameterElement qualifier(@Nullable String qualifier) {
      return (BeanParameterElement)InjectableElement.super.qualifier(qualifier);
   }

   @NonNull
   default BeanParameterElement qualifier(@NonNull AnnotationValue<?> qualifier) {
      return (BeanParameterElement)InjectableElement.super.qualifier(qualifier);
   }
}
