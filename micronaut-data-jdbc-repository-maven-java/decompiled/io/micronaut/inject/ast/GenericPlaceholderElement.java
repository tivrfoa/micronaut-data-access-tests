package io.micronaut.inject.ast;

import io.micronaut.core.annotation.NonNull;
import java.util.List;
import java.util.Optional;

public interface GenericPlaceholderElement extends ClassElement {
   @NonNull
   List<? extends ClassElement> getBounds();

   @NonNull
   String getVariableName();

   Optional<Element> getDeclaringElement();
}
