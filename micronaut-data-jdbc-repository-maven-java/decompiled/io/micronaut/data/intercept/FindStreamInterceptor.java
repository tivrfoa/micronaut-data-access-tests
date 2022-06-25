package io.micronaut.data.intercept;

import java.util.stream.Stream;

public interface FindStreamInterceptor<T> extends DataInterceptor<T, Stream<T>> {
}
