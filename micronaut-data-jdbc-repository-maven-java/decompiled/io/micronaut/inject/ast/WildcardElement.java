package io.micronaut.inject.ast;

import io.micronaut.core.annotation.NonNull;
import java.util.List;

public interface WildcardElement extends ClassElement {
   @NonNull
   List<? extends ClassElement> getUpperBounds();

   @NonNull
   List<? extends ClassElement> getLowerBounds();
}
