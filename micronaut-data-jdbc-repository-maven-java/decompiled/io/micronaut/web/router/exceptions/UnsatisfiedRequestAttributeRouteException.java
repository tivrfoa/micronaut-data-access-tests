package io.micronaut.web.router.exceptions;

import io.micronaut.core.type.Argument;

public final class UnsatisfiedRequestAttributeRouteException extends UnsatisfiedRouteException {
   private final String name;

   public UnsatisfiedRequestAttributeRouteException(String name, Argument<?> argument) {
      super("Required RequestAttribute [" + name + "] not specified", argument);
      this.name = name;
   }

   public String getRequestAttributeName() {
      return this.name;
   }
}
