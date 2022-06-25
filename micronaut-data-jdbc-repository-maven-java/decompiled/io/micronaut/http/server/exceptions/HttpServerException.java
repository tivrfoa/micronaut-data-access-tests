package io.micronaut.http.server.exceptions;

import io.micronaut.http.exceptions.HttpException;

public class HttpServerException extends HttpException {
   public HttpServerException(String message) {
      super(message);
   }

   public HttpServerException(String message, Throwable cause) {
      super(message, cause);
   }
}
