package io.micronaut.web.router.exceptions;

import io.micronaut.core.type.Argument;

public final class UnsatisfiedQueryValueRouteException extends UnsatisfiedRouteException {
   private final String name;

   public UnsatisfiedQueryValueRouteException(String name, Argument<?> argument) {
      super("Required QueryValue [" + name + "] not specified", argument);
      this.name = name;
   }

   public String getQueryParameterName() {
      return this.name;
   }
}
