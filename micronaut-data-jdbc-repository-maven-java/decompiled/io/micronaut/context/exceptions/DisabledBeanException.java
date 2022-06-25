package io.micronaut.context.exceptions;

public class DisabledBeanException extends RuntimeException {
   public DisabledBeanException(String reason) {
      super(reason);
   }

   public final synchronized Throwable fillInStackTrace() {
      return this;
   }
}
