package io.micronaut.http.multipart;

import io.micronaut.http.exceptions.HttpException;

public class MultipartException extends HttpException {
   public MultipartException(String message, Throwable cause) {
      super(message, cause);
   }

   public MultipartException(String message) {
      super(message);
   }
}
