package io.micronaut.core.type;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public interface TypeVariableResolver {
   default Map<String, Argument<?>> getTypeVariables() {
      return Collections.emptyMap();
   }

   default Argument[] getTypeParameters() {
      Collection<Argument<?>> values = this.getTypeVariables().values();
      return (Argument[])values.toArray(Argument.ZERO_ARGUMENTS);
   }

   default Optional<Argument<?>> getFirstTypeVariable() {
      Map<String, Argument<?>> typeVariables = this.getTypeVariables();
      return !typeVariables.isEmpty() ? Optional.of(typeVariables.values().iterator().next()) : Optional.empty();
   }

   default Optional<Argument<?>> getTypeVariable(String name) {
      Argument<?> argument = (Argument)this.getTypeVariables().get(name);
      return argument != null ? Optional.of(argument) : Optional.empty();
   }
}
