package io.micronaut.http.client.exceptions;

import io.micronaut.http.exceptions.HttpException;

public class HttpClientException extends HttpException {
   public HttpClientException(String message) {
      super(message);
   }

   public HttpClientException(String message, Throwable cause) {
      super(message, cause);
   }

   public HttpClientException(String message, Throwable cause, boolean shared) {
      super(message, cause, false, true);
      if (!shared) {
         throw new IllegalArgumentException("shared must be true");
      }
   }
}
