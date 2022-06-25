package io.micronaut.http.exceptions;

public abstract class HttpException extends RuntimeException {
   public HttpException() {
   }

   public HttpException(String message) {
      super(message);
   }

   public HttpException(String message, Throwable cause) {
      super(message, cause);
   }

   protected HttpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
