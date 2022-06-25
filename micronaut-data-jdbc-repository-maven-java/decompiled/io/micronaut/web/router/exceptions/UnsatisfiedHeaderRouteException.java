package io.micronaut.web.router.exceptions;

import io.micronaut.core.type.Argument;

public final class UnsatisfiedHeaderRouteException extends UnsatisfiedRouteException {
   private final String name;

   public UnsatisfiedHeaderRouteException(String name, Argument<?> argument) {
      super("Required Header [" + name + "] not specified", argument);
      this.name = name;
   }

   public String getHeaderName() {
      return this.name;
   }
}
