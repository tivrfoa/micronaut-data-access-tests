package io.micronaut.inject.writer;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.ast.Element;

public interface OriginatingElements {
   @NonNull
   Element[] getOriginatingElements();

   void addOriginatingElement(@NonNull Element element);

   static OriginatingElements of(Element... elements) {
      if (!Boolean.getBoolean("micronaut.static.originating.elements")) {
         return new DefaultOriginatingElements(elements);
      } else {
         for(Element element : elements) {
            StaticOriginatingElements.INSTANCE.addOriginatingElement(element);
         }

         return StaticOriginatingElements.INSTANCE;
      }
   }
}
