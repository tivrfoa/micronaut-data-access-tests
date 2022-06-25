package io.micronaut.web.router;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Internal
class ErrorRouteMatch<T, R> extends AbstractRouteMatch<T, R> {
   private final Throwable error;
   private final Map<String, Object> variables;

   ErrorRouteMatch(Throwable error, DefaultRouteBuilder.AbstractRoute abstractRoute, ConversionService<?> conversionService) {
      super(abstractRoute, conversionService);
      this.error = error;
      this.variables = new LinkedHashMap();

      for(Argument argument : this.getArguments()) {
         if (argument.getType().isInstance(error)) {
            this.variables.put(argument.getName(), error);
         }
      }

   }

   @Override
   public Collection<Argument> getRequiredArguments() {
      return (Collection<Argument>)Arrays.stream(this.getArguments())
         .filter(argument -> !argument.getType().isInstance(this.error))
         .collect(Collectors.toList());
   }

   @Override
   public Map<String, Object> getVariableValues() {
      return this.variables;
   }

   @Override
   public boolean isErrorRoute() {
      return true;
   }

   @Override
   protected RouteMatch<R> newFulfilled(Map<String, Object> newVariables, List<Argument> requiredArguments) {
      return new ErrorRouteMatch<T, R>(this.error, this.abstractRoute, this.conversionService) {
         @Override
         public Collection<Argument> getRequiredArguments() {
            return requiredArguments;
         }

         @Override
         public Map<String, Object> getVariableValues() {
            return newVariables;
         }
      };
   }

   @Override
   public RouteMatch<R> decorate(Function<RouteMatch<R>, R> executor) {
      final Map<String, Object> variables = this.getVariableValues();
      final Collection<Argument> arguments = this.getRequiredArguments();
      final RouteMatch thisRoute = this;
      return new ErrorRouteMatch(this.error, this.abstractRoute, this.conversionService) {
         @Override
         public Collection<Argument> getRequiredArguments() {
            return arguments;
         }

         @Override
         public T execute(Map argumentValues) {
            return (T)executor.apply(thisRoute);
         }

         @Override
         public Map<String, Object> getVariableValues() {
            return variables;
         }
      };
   }

   public String toString() {
      return this.abstractRoute.toString();
   }
}
