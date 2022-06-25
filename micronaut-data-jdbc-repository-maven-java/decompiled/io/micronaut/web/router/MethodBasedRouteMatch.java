package io.micronaut.web.router;

import io.micronaut.core.type.Argument;
import io.micronaut.inject.MethodExecutionHandle;
import java.util.Arrays;
import java.util.Collection;

public interface MethodBasedRouteMatch<T, R> extends RouteMatch<R>, MethodExecutionHandle<T, R> {
   @Override
   default Collection<Argument> getRequiredArguments() {
      return Arrays.asList(this.getArguments());
   }
}
