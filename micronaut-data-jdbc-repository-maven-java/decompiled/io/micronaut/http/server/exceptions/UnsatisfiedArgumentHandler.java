package io.micronaut.http.server.exceptions;

import io.micronaut.core.bind.exceptions.UnsatisfiedArgumentException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.response.Error;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
@Produces
public class UnsatisfiedArgumentHandler implements ExceptionHandler<UnsatisfiedArgumentException, HttpResponse> {
   private final ErrorResponseProcessor<?> responseProcessor;

   @Inject
   public UnsatisfiedArgumentHandler(ErrorResponseProcessor<?> responseProcessor) {
      this.responseProcessor = responseProcessor;
   }

   public HttpResponse handle(HttpRequest request, UnsatisfiedArgumentException exception) {
      MutableHttpResponse<?> response = HttpResponse.badRequest();
      return this.responseProcessor.processResponse(ErrorContext.builder(request).cause(exception).error(new Error() {
         @Override
         public String getMessage() {
            return exception.getMessage();
         }

         @Override
         public Optional<String> getPath() {
            return Optional.of('/' + exception.getArgument().getName());
         }
      }).build(), response);
   }
}
