package io.micronaut.core.util;

public interface Toggleable {
   default boolean isEnabled() {
      return true;
   }
}
