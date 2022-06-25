package io.micronaut.web.router;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class StatusRouteMatch<T, R> extends AbstractRouteMatch<T, R> {
   final HttpStatus httpStatus;
   private final ArrayList<Argument> requiredArguments;

   StatusRouteMatch(HttpStatus httpStatus, DefaultRouteBuilder.AbstractRoute abstractRoute, ConversionService<?> conversionService) {
      super(abstractRoute, conversionService);
      this.httpStatus = httpStatus;
      this.requiredArguments = new ArrayList(Arrays.asList(this.getArguments()));
   }

   @Override
   public Map<String, Object> getVariableValues() {
      return Collections.emptyMap();
   }

   @Override
   public Collection<Argument> getRequiredArguments() {
      return this.requiredArguments;
   }

   @Override
   public boolean isErrorRoute() {
      return true;
   }

   @Override
   public HttpStatus findStatus(HttpStatus defaultStatus) {
      return super.findStatus(this.httpStatus);
   }

   @Override
   protected RouteMatch<R> newFulfilled(Map<String, Object> newVariables, List<Argument> requiredArguments) {
      return new StatusRouteMatch<T, R>(this.httpStatus, this.abstractRoute, this.conversionService) {
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
      return new StatusRouteMatch<T, R>(this.httpStatus, this.abstractRoute, this.conversionService) {
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
}
