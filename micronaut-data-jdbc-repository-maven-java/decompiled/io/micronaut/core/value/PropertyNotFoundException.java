package io.micronaut.core.value;

public class PropertyNotFoundException extends ValueException {
   public PropertyNotFoundException(String name, Class type) {
      super("No property found for name [" + name + "] and type [" + type.getName() + "]");
   }
}
