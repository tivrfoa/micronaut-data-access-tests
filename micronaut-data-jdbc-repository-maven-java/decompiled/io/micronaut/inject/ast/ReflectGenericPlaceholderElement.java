package io.micronaut.inject.ast;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Internal
final class ReflectGenericPlaceholderElement extends ReflectTypeElement<TypeVariable<?>> implements GenericPlaceholderElement, ArrayableClassElement {
   private final int arrayDimensions;

   ReflectGenericPlaceholderElement(TypeVariable<?> typeVariable, int arrayDimensions) {
      super(typeVariable);
      this.arrayDimensions = arrayDimensions;
   }

   @Override
   public ClassElement withArrayDimensions(int arrayDimensions) {
      return new ReflectGenericPlaceholderElement(this.type, arrayDimensions);
   }

   @Override
   public int getArrayDimensions() {
      return this.arrayDimensions;
   }

   @NonNull
   @Override
   public List<? extends ClassElement> getBounds() {
      return (List<? extends ClassElement>)Arrays.stream(this.type.getBounds()).map(ClassElement::of).collect(Collectors.toList());
   }

   @NonNull
   @Override
   public String getVariableName() {
      return this.type.getName();
   }

   @Override
   public Optional<Element> getDeclaringElement() {
      GenericDeclaration declaration = this.type.getGenericDeclaration();
      return declaration instanceof Class ? Optional.of(ClassElement.of((Class<?>)declaration)) : Optional.empty();
   }
}
