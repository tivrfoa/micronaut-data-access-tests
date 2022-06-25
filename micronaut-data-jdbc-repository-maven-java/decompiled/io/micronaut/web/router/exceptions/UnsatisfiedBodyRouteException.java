package io.micronaut.web.router.exceptions;

import io.micronaut.core.type.Argument;

public final class UnsatisfiedBodyRouteException extends UnsatisfiedRouteException {
   private final String name;

   public UnsatisfiedBodyRouteException(String name, Argument<?> argument) {
      super("Required Body [" + name + "] not specified", argument);
      this.name = name;
   }

   public String getBodyVariableName() {
      return this.name;
   }
}
