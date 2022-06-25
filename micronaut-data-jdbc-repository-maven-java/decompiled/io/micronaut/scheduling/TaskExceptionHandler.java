package io.micronaut.scheduling;

import io.micronaut.core.exceptions.BeanExceptionHandler;

public interface TaskExceptionHandler<T, E extends Throwable> extends BeanExceptionHandler<T, E> {
}
