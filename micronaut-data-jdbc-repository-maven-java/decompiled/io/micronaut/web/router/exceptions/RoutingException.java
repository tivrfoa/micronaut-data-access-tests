package io.micronaut.web.router.exceptions;

import io.micronaut.context.exceptions.BeanContextException;

public class RoutingException extends BeanContextException {
   public RoutingException(String message, Throwable cause) {
      super(message, cause);
   }

   public RoutingException(String message) {
      super(message);
   }
}
