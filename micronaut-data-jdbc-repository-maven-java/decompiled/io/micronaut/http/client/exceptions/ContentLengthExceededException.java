package io.micronaut.http.client.exceptions;

public class ContentLengthExceededException extends HttpClientException {
   public ContentLengthExceededException(long maxLength, long receivedLength) {
      super("The received length [" + receivedLength + "] exceeds the maximum allowed content length [" + maxLength + "]");
   }

   public ContentLengthExceededException(long maxLength) {
      super("The received length exceeds the maximum allowed content length [" + maxLength + "]");
   }
}
