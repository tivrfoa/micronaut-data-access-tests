package io.micronaut.web.router;

import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.uri.UriMatchInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface UriRouteMatch<T, R> extends UriMatchInfo, MethodBasedRouteMatch<T, R> {
   UriRoute getRoute();

   default List<Argument> getRequiredArguments() {
      Argument[] arguments = this.getArguments();
      if (ArrayUtils.isNotEmpty(arguments)) {
         Map<String, Object> matchVariables = this.getVariableValues();
         List<Argument> actualArguments = new ArrayList(arguments.length);

         for(Argument argument : arguments) {
            if (!matchVariables.containsKey(argument.getName())) {
               actualArguments.add(argument);
            }
         }

         return actualArguments;
      } else {
         return Collections.emptyList();
      }
   }

   HttpMethod getHttpMethod();

   UriRouteMatch<T, R> fulfill(Map<String, Object> argumentValues);

   UriRouteMatch<T, R> decorate(Function<RouteMatch<R>, R> executor);
}
