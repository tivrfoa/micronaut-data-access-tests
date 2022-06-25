package io.micronaut.http.server.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.response.Error;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;

@Produces
@Singleton
@Requires(
   classes = {JsonProcessingException.class}
)
public class JsonExceptionHandler implements ExceptionHandler<JsonProcessingException, Object> {
   private final ErrorResponseProcessor<?> responseProcessor;

   @Inject
   public JsonExceptionHandler(ErrorResponseProcessor<?> responseProcessor) {
      this.responseProcessor = responseProcessor;
   }

   public Object handle(HttpRequest request, JsonProcessingException exception) {
      MutableHttpResponse<Object> response = HttpResponse.status(HttpStatus.BAD_REQUEST, "Invalid JSON");
      return this.responseProcessor.processResponse(ErrorContext.builder(request).cause(exception).error(new Error() {
         @Override
         public String getMessage() {
            return "Invalid JSON: " + exception.getMessage();
         }

         @Override
         public Optional<String> getTitle() {
            return Optional.of("Invalid JSON");
         }
      }).build(), response);
   }
}
