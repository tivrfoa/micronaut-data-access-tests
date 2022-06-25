package io.micronaut.core.type;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

public interface Executable<T, R> extends AnnotationMetadataProvider {
   @NonNull
   Class<T> getDeclaringType();

   @NonNull
   Argument<?>[] getArguments();

   @Nullable
   R invoke(@NonNull T instance, Object... arguments);
}
