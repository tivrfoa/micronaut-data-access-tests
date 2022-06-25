package io.micronaut.management.endpoint.routes;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.UriRoute;
import java.util.Comparator;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Endpoint("routes")
public class RoutesEndpoint {
   private final Router router;
   private final RouteDataCollector routeDataCollector;

   public RoutesEndpoint(Router router, RouteDataCollector routeDataCollector) {
      this.router = router;
      this.routeDataCollector = routeDataCollector;
   }

   @Read
   @SingleResult
   public Publisher getRoutes() {
      Stream<UriRoute> uriRoutes = this.router
         .uriRoutes()
         .sorted(Comparator.comparing(r -> r.getUriMatchTemplate().toPathString()).thenComparing(UriRoute::getHttpMethodName));
      return Mono.from(this.routeDataCollector.getData(uriRoutes));
   }
}
