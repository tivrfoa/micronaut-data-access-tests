package io.micronaut.http.client.exceptions;

public class EmptyResponseException extends HttpClientException {
   public EmptyResponseException() {
      super("HTTP Server returned an empty (and invalid) response body");
   }
}
