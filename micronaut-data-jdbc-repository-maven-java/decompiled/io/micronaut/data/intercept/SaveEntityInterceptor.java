package io.micronaut.data.intercept;

import io.micronaut.core.annotation.Blocking;

@Blocking
public interface SaveEntityInterceptor<T> extends DataInterceptor<T, Object> {
}
