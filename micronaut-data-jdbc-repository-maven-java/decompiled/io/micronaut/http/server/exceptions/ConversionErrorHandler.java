package io.micronaut.http.server.exceptions;

import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.response.Error;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
@Produces
public class ConversionErrorHandler implements ExceptionHandler<ConversionErrorException, HttpResponse> {
   private final ErrorResponseProcessor<?> responseProcessor;

   @Inject
   public ConversionErrorHandler(ErrorResponseProcessor<?> responseProcessor) {
      this.responseProcessor = responseProcessor;
   }

   public HttpResponse handle(HttpRequest request, ConversionErrorException exception) {
      return this.responseProcessor.processResponse(ErrorContext.builder(request).cause(exception).error(new Error() {
         @Override
         public Optional<String> getPath() {
            return Optional.of('/' + exception.getArgument().getName());
         }

         @Override
         public String getMessage() {
            return exception.getMessage();
         }
      }).build(), HttpResponse.badRequest());
   }
}
