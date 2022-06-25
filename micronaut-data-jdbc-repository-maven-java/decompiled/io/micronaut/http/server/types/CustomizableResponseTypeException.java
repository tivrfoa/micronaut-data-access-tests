package io.micronaut.http.server.types;

public class CustomizableResponseTypeException extends RuntimeException {
   public CustomizableResponseTypeException(String msg) {
      super(msg);
   }

   public CustomizableResponseTypeException(String msg, Throwable cause) {
      super(msg, cause);
   }
}
