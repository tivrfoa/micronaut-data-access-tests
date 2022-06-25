package io.micronaut.http.server.exceptions;

import io.micronaut.context.annotation.Executable;
import io.micronaut.http.HttpRequest;

public interface ExceptionHandler<T extends Throwable, R> {
   @Executable
   R handle(HttpRequest request, T exception);
}
