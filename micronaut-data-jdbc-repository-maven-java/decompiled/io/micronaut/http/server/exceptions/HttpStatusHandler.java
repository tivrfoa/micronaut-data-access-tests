package io.micronaut.http.server.exceptions;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
@Produces
public class HttpStatusHandler implements ExceptionHandler<HttpStatusException, HttpResponse> {
   private final ErrorResponseProcessor<?> responseProcessor;

   @Inject
   public HttpStatusHandler(ErrorResponseProcessor<?> responseProcessor) {
      this.responseProcessor = responseProcessor;
   }

   public HttpResponse handle(HttpRequest request, HttpStatusException exception) {
      MutableHttpResponse<?> response = HttpResponse.status(exception.getStatus());
      Optional<Object> body = exception.getBody();
      return body.isPresent()
         ? response.body(body.get())
         : this.responseProcessor.processResponse(ErrorContext.builder(request).cause(exception).errorMessage(exception.getMessage()).build(), response);
   }
}
