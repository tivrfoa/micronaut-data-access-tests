package io.micronaut.web.router.exceptions;

import io.micronaut.core.type.Argument;

public final class UnsatisfiedPathVariableRouteException extends UnsatisfiedRouteException {
   private final String name;

   public UnsatisfiedPathVariableRouteException(String name, Argument<?> argument) {
      super("Required PathVariable [" + name + "] not specified", argument);
      this.name = name;
   }

   public String getPathVariableName() {
      return this.name;
   }
}
