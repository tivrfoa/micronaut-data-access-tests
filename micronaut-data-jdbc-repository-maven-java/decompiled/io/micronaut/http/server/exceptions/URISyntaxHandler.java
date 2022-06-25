package io.micronaut.http.server.exceptions;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.response.Error;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URISyntaxException;
import java.util.Optional;

@Singleton
@Produces
public class URISyntaxHandler implements ExceptionHandler<URISyntaxException, HttpResponse> {
   private final ErrorResponseProcessor<?> responseProcessor;

   @Inject
   public URISyntaxHandler(ErrorResponseProcessor<?> responseProcessor) {
      this.responseProcessor = responseProcessor;
   }

   public HttpResponse handle(HttpRequest request, URISyntaxException exception) {
      return this.responseProcessor.processResponse(ErrorContext.builder(request).cause(exception).error(new Error() {
         @Override
         public String getMessage() {
            return "Malformed URI: " + exception.getMessage();
         }

         @Override
         public Optional<String> getTitle() {
            return Optional.of("Malformed URI");
         }
      }).build(), HttpResponse.badRequest());
   }
}
