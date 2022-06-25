package io.micronaut.inject.writer;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.ast.Element;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Internal
final class DefaultOriginatingElements implements OriginatingElements {
   private final Map<String, Element> originatingElements;

   DefaultOriginatingElements(Element... originatingElements) {
      this.originatingElements = new LinkedHashMap(originatingElements != null ? originatingElements.length : 5);
      if (originatingElements != null) {
         for(Element originatingElement : originatingElements) {
            if (originatingElement != null) {
               this.originatingElements.put(originatingElement.getName(), originatingElement);
            }
         }
      }

   }

   @Override
   public void addOriginatingElement(@NonNull Element element) {
      Objects.requireNonNull(element, "Element cannot be null");
      this.originatingElements.put(element.getName(), element);
   }

   @NonNull
   @Override
   public Element[] getOriginatingElements() {
      return (Element[])this.originatingElements.values().toArray(Element.EMPTY_ELEMENT_ARRAY);
   }
}
