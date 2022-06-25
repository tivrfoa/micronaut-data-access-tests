package io.micronaut.http.server.types;

import io.micronaut.http.MutableHttpResponse;

public interface CustomizableResponseType {
   default void process(MutableHttpResponse<?> response) {
   }
}
