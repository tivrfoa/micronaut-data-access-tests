package io.micronaut.inject.ast;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Internal
final class ReflectWildcardElement extends ReflectTypeElement<WildcardType> implements WildcardElement {
   ReflectWildcardElement(WildcardType type) {
      super(type);
   }

   @NonNull
   @Override
   public ClassElement toArray() {
      throw new UnsupportedOperationException();
   }

   @NonNull
   @Override
   public ClassElement fromArray() {
      throw new UnsupportedOperationException();
   }

   @NonNull
   @Override
   public List<? extends ClassElement> getUpperBounds() {
      return (List<? extends ClassElement>)Arrays.stream(this.type.getUpperBounds()).map(ClassElement::of).collect(Collectors.toList());
   }

   @NonNull
   @Override
   public List<? extends ClassElement> getLowerBounds() {
      return (List<? extends ClassElement>)Arrays.stream(this.type.getLowerBounds()).map(ClassElement::of).collect(Collectors.toList());
   }
}
