package io.micronaut.core.bind;

import io.micronaut.core.type.Argument;

public interface TypeArgumentBinder<T, S> extends ArgumentBinder<T, S> {
   Argument<T> argumentType();
}
