package io.micronaut.http.server.exceptions.response;

import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.MutableHttpResponse;

@DefaultImplementation(HateoasErrorResponseProcessor.class)
public interface ErrorResponseProcessor<T> {
   @NonNull
   MutableHttpResponse<T> processResponse(@NonNull ErrorContext errorContext, @NonNull MutableHttpResponse<?> baseResponse);
}
