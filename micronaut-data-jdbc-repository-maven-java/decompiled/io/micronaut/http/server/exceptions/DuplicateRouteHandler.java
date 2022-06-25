package io.micronaut.http.server.exceptions;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import io.micronaut.web.router.exceptions.DuplicateRouteException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@Produces
public class DuplicateRouteHandler implements ExceptionHandler<DuplicateRouteException, HttpResponse> {
   private final ErrorResponseProcessor<?> responseProcessor;

   @Inject
   public DuplicateRouteHandler(ErrorResponseProcessor<?> responseProcessor) {
      this.responseProcessor = responseProcessor;
   }

   public HttpResponse handle(HttpRequest request, DuplicateRouteException exception) {
      MutableHttpResponse<?> response = HttpResponse.badRequest();
      return this.responseProcessor.processResponse(ErrorContext.builder(request).cause(exception).errorMessage(exception.getMessage()).build(), response);
   }
}
