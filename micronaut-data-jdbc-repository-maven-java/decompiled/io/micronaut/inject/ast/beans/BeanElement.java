package io.micronaut.inject.ast.beans;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.visitor.VisitorContext;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface BeanElement extends Element {
   @NonNull
   Collection<Element> getInjectionPoints();

   @NonNull
   Element getOriginatingElement();

   @NonNull
   ClassElement getDeclaringClass();

   @NonNull
   Element getProducingElement();

   @NonNull
   Set<ClassElement> getBeanTypes();

   @NonNull
   Optional<String> getScope();

   @NonNull
   Collection<String> getQualifiers();

   @NonNull
   default BeanElementBuilder addAssociatedBean(@NonNull ClassElement type, @NonNull VisitorContext visitorContext) {
      throw new UnsupportedOperationException("Element of type [" + this.getClass() + "] does not support adding associated beans at compilation time");
   }
}
