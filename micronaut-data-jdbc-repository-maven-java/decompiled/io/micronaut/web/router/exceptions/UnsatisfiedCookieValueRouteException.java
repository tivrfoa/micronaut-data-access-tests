package io.micronaut.web.router.exceptions;

import io.micronaut.core.type.Argument;

public final class UnsatisfiedCookieValueRouteException extends UnsatisfiedRouteException {
   private final String name;

   public UnsatisfiedCookieValueRouteException(String name, Argument<?> argument) {
      super("Required CookieValue [" + name + "] not specified", argument);
      this.name = name;
   }

   public String getCookieName() {
      return this.name;
   }
}
