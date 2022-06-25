package io.micronaut.http.server.exceptions.response;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Internal
final class DefaultErrorContext implements ErrorContext {
   private final HttpRequest<?> request;
   private final Throwable cause;
   private final List<Error> jsonErrors;

   private DefaultErrorContext(@NonNull HttpRequest<?> request, @Nullable Throwable cause, @NonNull List<Error> jsonErrors) {
      this.request = request;
      this.cause = cause;
      this.jsonErrors = jsonErrors;
   }

   @NonNull
   @Override
   public HttpRequest<?> getRequest() {
      return this.request;
   }

   @NonNull
   @Override
   public Optional<Throwable> getRootCause() {
      return Optional.ofNullable(this.cause);
   }

   @NonNull
   @Override
   public List<Error> getErrors() {
      return this.jsonErrors;
   }

   public static DefaultErrorContext.Builder builder(@NonNull HttpRequest<?> request) {
      return new DefaultErrorContext.Builder(request);
   }

   private static final class Builder implements ErrorContext.Builder {
      private final HttpRequest<?> request;
      private Throwable cause;
      private final List<Error> jsonErrors = new ArrayList();

      private Builder(@NonNull HttpRequest<?> request) {
         this.request = request;
      }

      @NonNull
      public DefaultErrorContext.Builder cause(@Nullable Throwable cause) {
         this.cause = cause;
         return this;
      }

      @NonNull
      public DefaultErrorContext.Builder errorMessage(@NonNull String message) {
         this.jsonErrors.add((Error)() -> message);
         return this;
      }

      @NonNull
      public DefaultErrorContext.Builder error(@NonNull Error error) {
         this.jsonErrors.add(error);
         return this;
      }

      @NonNull
      public DefaultErrorContext.Builder errorMessages(@NonNull List<String> errors) {
         for(String error : errors) {
            this.errorMessage(error);
         }

         return this;
      }

      @NonNull
      public DefaultErrorContext.Builder errors(@NonNull List<Error> errors) {
         this.jsonErrors.addAll(errors);
         return this;
      }

      @NonNull
      @Override
      public ErrorContext build() {
         return new DefaultErrorContext(this.request, this.cause, this.jsonErrors);
      }
   }
}
