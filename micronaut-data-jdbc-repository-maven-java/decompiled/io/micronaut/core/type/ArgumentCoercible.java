package io.micronaut.core.type;

import io.micronaut.core.annotation.NonNull;

public interface ArgumentCoercible<T> {
   @NonNull
   Argument<T> asArgument();
}
