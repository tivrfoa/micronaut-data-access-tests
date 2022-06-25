package io.micronaut.inject.ast.beans;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.Element;
import java.util.Objects;

public interface ConfigurableElement extends Element {
   @NonNull
   ConfigurableElement typeArguments(@NonNull ClassElement... types);

   @NonNull
   default ConfigurableElement qualifier(@Nullable String qualifier) {
      return this.qualifier(AnnotationValue.builder("javax.inject.Named").value(qualifier).build());
   }

   @NonNull
   default ConfigurableElement qualifier(@NonNull AnnotationValue<?> qualifier) {
      Objects.requireNonNull(qualifier, "Qualifier cannot be null");
      this.annotate(qualifier.getAnnotationName(), builder -> builder.members(qualifier.getValues()));
      return this;
   }
}
