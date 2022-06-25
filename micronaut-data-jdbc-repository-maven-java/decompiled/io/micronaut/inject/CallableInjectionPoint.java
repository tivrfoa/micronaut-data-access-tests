package io.micronaut.inject;

import io.micronaut.core.type.Argument;

public interface CallableInjectionPoint<T> extends InjectionPoint<T> {
   Argument<?>[] getArguments();
}
