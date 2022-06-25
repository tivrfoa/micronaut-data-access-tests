package io.micronaut.management.endpoint.routes.impl;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.MediaType;
import io.micronaut.management.endpoint.routes.RouteData;
import io.micronaut.management.endpoint.routes.RouteDataCollector;
import io.micronaut.management.endpoint.routes.RoutesEndpoint;
import io.micronaut.web.router.UriRoute;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Singleton
@Requires(
   beans = {RoutesEndpoint.class}
)
public class DefaultRouteDataCollector implements RouteDataCollector<Map<String, Object>> {
   private final RouteData routeData;

   public DefaultRouteDataCollector(RouteData routeData) {
      this.routeData = routeData;
   }

   @Override
   public Publisher<Map<String, Object>> getData(Stream<UriRoute> routes) {
      List<UriRoute> routeList = (List)routes.collect(Collectors.toList());
      return Flux.fromIterable(routeList).collectMap(this::getRouteKey, this.routeData::getData);
   }

   protected String getRouteKey(UriRoute route) {
      String produces = (String)route.getProduces().stream().map(MediaType::toString).collect(Collectors.joining(" || "));
      return "{[" + route.getUriMatchTemplate() + "],method=[" + route.getHttpMethodName() + "],produces=[" + produces + "]}";
   }
}
