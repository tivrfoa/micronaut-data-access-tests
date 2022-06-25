package io.micronaut.inject.visitor;

public interface VisitorConfiguration {
   VisitorConfiguration DEFAULT = new VisitorConfiguration() {
   };

   default boolean includeTypeLevelAnnotationsInGenericArguments() {
      return true;
   }
}
