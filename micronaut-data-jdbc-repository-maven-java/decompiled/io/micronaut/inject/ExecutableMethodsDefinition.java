package io.micronaut.inject;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Internal
public interface ExecutableMethodsDefinition<T> {
   @NonNull
   <R> Optional<ExecutableMethod<T, R>> findMethod(@NonNull String name, @NonNull Class<?>... argumentTypes);

   @NonNull
   <R> Stream<ExecutableMethod<T, R>> findPossibleMethods(@NonNull String name);

   @NonNull
   Collection<ExecutableMethod<T, ?>> getExecutableMethods();
}
