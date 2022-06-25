package io.micronaut.http.exceptions;

public class ContentLengthExceededException extends HttpException {
   public ContentLengthExceededException(String message) {
      super(message);
   }

   public ContentLengthExceededException(String message, Throwable cause) {
      super(message, cause);
   }

   public ContentLengthExceededException(long advertisedLength, long receivedLength) {
      this("The content length [" + receivedLength + "] exceeds the maximum allowed content length [" + advertisedLength + "]");
   }
}
