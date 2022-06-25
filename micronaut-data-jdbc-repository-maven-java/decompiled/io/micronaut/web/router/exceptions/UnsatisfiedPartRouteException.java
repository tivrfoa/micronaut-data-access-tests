package io.micronaut.web.router.exceptions;

import io.micronaut.core.type.Argument;

public final class UnsatisfiedPartRouteException extends UnsatisfiedRouteException {
   private final String name;

   public UnsatisfiedPartRouteException(String name, Argument<?> argument) {
      super("Required Part [" + name + "] not specified", argument);
      this.name = name;
   }

   public String getPartName() {
      return this.name;
   }
}
