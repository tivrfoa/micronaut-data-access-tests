package io.micronaut.core.exceptions;

@FunctionalInterface
public interface ExceptionHandler<T extends Throwable> {
   void handle(T exception);
}
