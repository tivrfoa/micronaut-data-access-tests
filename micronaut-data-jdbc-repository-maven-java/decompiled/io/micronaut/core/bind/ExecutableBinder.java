package io.micronaut.core.bind;

import io.micronaut.core.bind.exceptions.UnsatisfiedArgumentException;
import io.micronaut.core.type.Executable;

public interface ExecutableBinder<S> {
   <T, R> BoundExecutable<T, R> bind(Executable<T, R> target, ArgumentBinderRegistry<S> registry, S source) throws UnsatisfiedArgumentException;

   <T, R> BoundExecutable<T, R> tryBind(Executable<T, R> target, ArgumentBinderRegistry<S> registry, S source);
}
