package io.micronaut.http.client.exceptions;

public final class ReadTimeoutException extends HttpClientException {
   public static final ReadTimeoutException TIMEOUT_EXCEPTION = new ReadTimeoutException();

   private ReadTimeoutException() {
      super("Read Timeout", null, true);
   }
}
