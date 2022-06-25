package io.micronaut.http.codec;

import io.micronaut.http.exceptions.HttpException;

public class CodecException extends HttpException {
   public CodecException(String message) {
      super(message);
   }

   public CodecException(String message, Throwable cause) {
      super(message, cause);
   }
}
