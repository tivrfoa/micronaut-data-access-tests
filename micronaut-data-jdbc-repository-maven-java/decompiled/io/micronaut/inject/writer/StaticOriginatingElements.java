package io.micronaut.inject.writer;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.ast.Element;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Internal
public final class StaticOriginatingElements implements OriginatingElements {
   public static final StaticOriginatingElements INSTANCE = new StaticOriginatingElements();
   private final Map<String, Element> originatingElements = new LinkedHashMap(5);

   private StaticOriginatingElements() {
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

   public void clear() {
      this.originatingElements.clear();
   }
}
