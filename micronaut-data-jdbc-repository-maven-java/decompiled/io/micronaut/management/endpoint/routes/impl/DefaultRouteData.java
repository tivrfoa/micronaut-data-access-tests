package io.micronaut.management.endpoint.routes.impl;

import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.MethodExecutionHandle;
import io.micronaut.management.endpoint.routes.RouteData;
import io.micronaut.management.endpoint.routes.RoutesEndpoint;
import io.micronaut.web.router.MethodBasedRoute;
import io.micronaut.web.router.UriRoute;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Requires(
   beans = {RoutesEndpoint.class}
)
public class DefaultRouteData implements RouteData<Map<String, String>> {
   public Map<String, String> getData(UriRoute route) {
      Map<String, String> values = new LinkedHashMap(1);
      if (route instanceof MethodBasedRoute) {
         values.put("method", this.getMethodString(((MethodBasedRoute)route).getTargetMethod()));
      }

      return values;
   }

   protected String getMethodString(MethodExecutionHandle targetMethod) {
      return targetMethod.getReturnType().asArgument().getTypeString(false)
         + " "
         + targetMethod.getDeclaringType().getName()
         + '.'
         + targetMethod.getMethodName()
         + "("
         + (String)Arrays.stream(targetMethod.getArguments())
            .map(argument -> argument.getType().getName() + " " + argument.getName())
            .collect(Collectors.joining(", "))
         + ")";
   }
}
