package io.micronaut.http.server.exceptions;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.response.Error;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Produces
public class UnsatisfiedRouteHandler implements ExceptionHandler<UnsatisfiedRouteException, HttpResponse> {
   private static final Logger LOG = LoggerFactory.getLogger(UnsatisfiedRouteHandler.class);
   private final ErrorResponseProcessor<?> responseProcessor;

   @Inject
   public UnsatisfiedRouteHandler(ErrorResponseProcessor<?> responseProcessor) {
      this.responseProcessor = responseProcessor;
   }

   public HttpResponse handle(HttpRequest request, UnsatisfiedRouteException exception) {
      if (LOG.isTraceEnabled()) {
         LOG.trace("{} (Bad Request): {}", request, exception.getMessage());
      }

      return this.responseProcessor.processResponse(ErrorContext.builder(request).cause(exception).error(new Error() {
         @Override
         public String getMessage() {
            return exception.getMessage();
         }

         @Override
         public Optional<String> getPath() {
            return Optional.of('/' + exception.getArgument().getName());
         }
      }).build(), HttpResponse.badRequest());
   }
}
