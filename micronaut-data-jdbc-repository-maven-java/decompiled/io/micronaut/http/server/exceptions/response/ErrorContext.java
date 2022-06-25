package io.micronaut.http.server.exceptions.response;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import java.util.List;
import java.util.Optional;

public interface ErrorContext {
   @NonNull
   HttpRequest<?> getRequest();

   @NonNull
   Optional<Throwable> getRootCause();

   @NonNull
   List<Error> getErrors();

   default boolean hasErrors() {
      return !this.getErrors().isEmpty();
   }

   @NonNull
   static ErrorContext.Builder builder(@NonNull HttpRequest<?> request) {
      return DefaultErrorContext.builder(request);
   }

   public interface Builder {
      @NonNull
      ErrorContext.Builder cause(@Nullable Throwable cause);

      @NonNull
      ErrorContext.Builder errorMessage(@NonNull String message);

      @NonNull
      ErrorContext.Builder error(@NonNull Error error);

      @NonNull
      ErrorContext.Builder errorMessages(@NonNull List<String> errors);

      @NonNull
      ErrorContext.Builder errors(@NonNull List<Error> errors);

      @NonNull
      ErrorContext build();
   }
}
