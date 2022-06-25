package io.micronaut.inject.ast.beans;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.ast.FieldElement;

public interface BeanFieldElement extends FieldElement, InjectableElement {
   default BeanFieldElement inject() {
      this.annotate("javax.inject.Inject");
      return this;
   }

   default BeanFieldElement injectValue(String expression) {
      return (BeanFieldElement)InjectableElement.super.injectValue(expression);
   }

   @NonNull
   default BeanFieldElement qualifier(@Nullable String qualifier) {
      return (BeanFieldElement)InjectableElement.super.qualifier(qualifier);
   }

   @NonNull
   default BeanFieldElement qualifier(@NonNull AnnotationValue<?> qualifier) {
      return (BeanFieldElement)InjectableElement.super.qualifier(qualifier);
   }
}
