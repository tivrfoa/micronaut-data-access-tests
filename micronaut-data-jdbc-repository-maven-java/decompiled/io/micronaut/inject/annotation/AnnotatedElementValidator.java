package io.micronaut.inject.annotation;

import io.micronaut.core.annotation.AnnotatedElement;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.util.Set;

public interface AnnotatedElementValidator {
   @NonNull
   Set<String> validatedAnnotatedElement(@NonNull AnnotatedElement element, @Nullable Object value);
}
