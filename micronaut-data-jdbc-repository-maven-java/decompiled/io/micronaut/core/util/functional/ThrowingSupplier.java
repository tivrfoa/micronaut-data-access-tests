package io.micronaut.core.util.functional;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> {
   T get() throws E;
}
