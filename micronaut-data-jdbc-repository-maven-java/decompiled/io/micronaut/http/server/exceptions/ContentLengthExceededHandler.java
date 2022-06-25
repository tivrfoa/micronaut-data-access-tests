package io.micronaut.http.server.exceptions;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.exceptions.ContentLengthExceededException;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@Produces
public class ContentLengthExceededHandler implements ExceptionHandler<ContentLengthExceededException, HttpResponse> {
   private final ErrorResponseProcessor<?> responseProcessor;

   @Inject
   public ContentLengthExceededHandler(ErrorResponseProcessor<?> responseProcessor) {
      this.responseProcessor = responseProcessor;
   }

   public HttpResponse handle(HttpRequest request, ContentLengthExceededException exception) {
      MutableHttpResponse<?> response = HttpResponse.status(HttpStatus.REQUEST_ENTITY_TOO_LARGE);
      return this.responseProcessor.processResponse(ErrorContext.builder(request).cause(exception).errorMessage(exception.getMessage()).build(), response);
   }
}
