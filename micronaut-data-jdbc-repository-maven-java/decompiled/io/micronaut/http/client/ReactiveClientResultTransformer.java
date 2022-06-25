package io.micronaut.http.client;

import io.micronaut.core.annotation.Indexed;

@Indexed(ReactiveClientResultTransformer.class)
public interface ReactiveClientResultTransformer {
   Object transform(Object publisherResult);
}
