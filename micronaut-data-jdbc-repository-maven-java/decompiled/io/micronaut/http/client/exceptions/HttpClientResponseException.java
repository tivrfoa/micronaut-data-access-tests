package io.micronaut.http.client.exceptions;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpResponseProvider;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import java.util.Optional;

public class HttpClientResponseException extends HttpClientException implements HttpResponseProvider {
   private final HttpResponse<?> response;
   private final HttpClientErrorDecoder errorDecoder;

   public HttpClientResponseException(String message, HttpResponse<?> response) {
      this(message, null, response);
   }

   public HttpClientResponseException(String message, Throwable cause, HttpResponse<?> response) {
      this(message, cause, response, HttpClientErrorDecoder.DEFAULT);
   }

   public HttpClientResponseException(String message, Throwable cause, HttpResponse<?> response, HttpClientErrorDecoder errorDecoder) {
      super(message, cause);
      this.errorDecoder = errorDecoder;
      this.response = response;
      this.initResponse(response);
   }

   public String getMessage() {
      Optional<Argument<?>> errorType = Optional.ofNullable(this.getErrorType(this.response));
      return errorType.isPresent()
         ? (String)this.getResponse().getBody((Argument)errorType.get()).flatMap(this.errorDecoder::getMessage).orElse(super.getMessage())
         : super.getMessage();
   }

   @Override
   public HttpResponse<?> getResponse() {
      return this.response;
   }

   public HttpStatus getStatus() {
      return this.getResponse().getStatus();
   }

   private void initResponse(HttpResponse<?> response) {
      Argument<?> errorType = this.getErrorType(response);
      if (errorType != null) {
         response.getBody(errorType);
      } else {
         response.getBody(String.class);
      }

   }

   private Argument<?> getErrorType(HttpResponse<?> response) {
      Optional<MediaType> contentType = response.getContentType();
      Argument<?> errorType = null;
      if (contentType.isPresent() && response.getStatus().getCode() > 399) {
         MediaType mediaType = (MediaType)contentType.get();
         if (this.errorDecoder != null) {
            errorType = this.errorDecoder.getErrorType(mediaType);
         }
      }

      return errorType;
   }
}
