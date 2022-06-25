package io.micronaut.management.endpoint;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.web.router.MethodBasedRouteMatch;
import io.micronaut.web.router.RouteMatch;
import io.micronaut.web.router.RouteMatchUtils;
import java.util.Map;
import java.util.Optional;
import org.reactivestreams.Publisher;

@Filter({"/**"})
public class EndpointsFilter extends OncePerRequestHttpServerFilter {
   private final Map<ExecutableMethod, Boolean> endpointMethods;

   public EndpointsFilter(EndpointSensitivityProcessor endpointSensitivityProcessor) {
      this.endpointMethods = endpointSensitivityProcessor.getEndpointMethods();
   }

   @Override
   protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
      Optional<RouteMatch> routeMatch = RouteMatchUtils.findRouteMatch(request);
      if (routeMatch.isPresent() && routeMatch.get() instanceof MethodBasedRouteMatch) {
         ExecutableMethod method = ((MethodBasedRouteMatch)routeMatch.get()).getExecutableMethod();
         if (this.endpointMethods.getOrDefault(method, false)) {
            return Publishers.just(HttpResponse.status(HttpStatus.UNAUTHORIZED));
         }
      }

      return chain.proceed(request);
   }

   @Override
   public int getOrder() {
      return ServerFilterPhase.SECURITY.order();
   }
}
