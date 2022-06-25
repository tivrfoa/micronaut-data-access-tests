package io.micronaut.context.exceptions;

public class NoSuchMessageException extends BeanContextException {
   public NoSuchMessageException(String code) {
      super("No message exists for the given code: " + code);
   }
}
