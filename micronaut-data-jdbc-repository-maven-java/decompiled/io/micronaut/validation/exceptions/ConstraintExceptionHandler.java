package io.micronaut.validation.exceptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;

@Produces
@Singleton
@Requires(
   classes = {ConstraintViolationException.class, ExceptionHandler.class}
)
public class ConstraintExceptionHandler implements ExceptionHandler<ConstraintViolationException, HttpResponse<?>> {
   private final ErrorResponseProcessor<?> responseProcessor;

   @Inject
   public ConstraintExceptionHandler(ErrorResponseProcessor<?> responseProcessor) {
      this.responseProcessor = responseProcessor;
   }

   public HttpResponse<?> handle(HttpRequest request, ConstraintViolationException exception) {
      Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();
      MutableHttpResponse<?> response = HttpResponse.badRequest();
      ErrorContext.Builder contextBuilder = ErrorContext.builder(request).cause(exception);
      return constraintViolations != null && !constraintViolations.isEmpty()
         ? this.responseProcessor
            .processResponse(
               contextBuilder.errorMessages(
                     (List<String>)exception.getConstraintViolations().stream().map(this::buildMessage).sorted().collect(Collectors.toList())
                  )
                  .build(),
               response
            )
         : this.responseProcessor
            .processResponse(
               contextBuilder.errorMessage(exception.getMessage() == null ? HttpStatus.BAD_REQUEST.getReason() : exception.getMessage()).build(), response
            );
   }

   protected String buildMessage(ConstraintViolation violation) {
      Path propertyPath = violation.getPropertyPath();
      StringBuilder message = new StringBuilder();
      Iterator<Path.Node> i = propertyPath.iterator();

      while(i.hasNext()) {
         Path.Node node = (Path.Node)i.next();
         if (node.getKind() != ElementKind.METHOD && node.getKind() != ElementKind.CONSTRUCTOR) {
            message.append(node.getName());
            if (node.getIndex() != null) {
               message.append(String.format("[%d]", node.getIndex()));
            }

            if (i.hasNext()) {
               message.append('.');
            }
         }
      }

      message.append(": ").append(violation.getMessage());
      return message.toString();
   }
}
