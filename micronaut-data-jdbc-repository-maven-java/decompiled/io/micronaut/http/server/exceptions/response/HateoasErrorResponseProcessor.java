package io.micronaut.http.server.exceptions.response;

import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.hateoas.Resource;
import io.micronaut.jackson.JacksonConfiguration;
import io.micronaut.json.JsonConfiguration;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Secondary
public class HateoasErrorResponseProcessor implements ErrorResponseProcessor<JsonError> {
   private final boolean alwaysSerializeErrorsAsList;

   public HateoasErrorResponseProcessor(JsonConfiguration jacksonConfiguration) {
      this.alwaysSerializeErrorsAsList = jacksonConfiguration.isAlwaysSerializeErrorsAsList();
   }

   public HateoasErrorResponseProcessor(JacksonConfiguration jacksonConfiguration) {
      this((JsonConfiguration)jacksonConfiguration);
   }

   @NonNull
   @Override
   public MutableHttpResponse<JsonError> processResponse(@NonNull ErrorContext errorContext, @NonNull MutableHttpResponse<?> response) {
      if (errorContext.getRequest().getMethod() == HttpMethod.HEAD) {
         return response;
      } else {
         JsonError error;
         if (!errorContext.hasErrors()) {
            error = new JsonError(response.getStatus().getReason());
         } else if (errorContext.getErrors().size() == 1 && !this.alwaysSerializeErrorsAsList) {
            Error jsonError = (Error)errorContext.getErrors().get(0);
            error = new JsonError(jsonError.getMessage());
            jsonError.getPath().ifPresent(error::path);
         } else {
            error = new JsonError(response.getStatus().getReason());
            List<Resource> errors = new ArrayList();

            for(Error jsonError : errorContext.getErrors()) {
               errors.add(new JsonError(jsonError.getMessage()).path((String)jsonError.getPath().orElse(null)));
            }

            error.embedded("errors", errors);
         }

         error.link(Link.SELF, Link.of(errorContext.getRequest().getUri()));
         return response.body(error).contentType(MediaType.APPLICATION_JSON_TYPE);
      }
   }
}
