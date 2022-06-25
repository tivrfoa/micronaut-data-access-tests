package io.micronaut.http.bind.binders;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.http.HttpRequest;

@Indexed(RequestArgumentBinder.class)
public interface RequestArgumentBinder<T> extends ArgumentBinder<T, HttpRequest<?>> {
}
