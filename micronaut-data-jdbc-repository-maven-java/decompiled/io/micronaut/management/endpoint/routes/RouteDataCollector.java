package io.micronaut.management.endpoint.routes;

import io.micronaut.web.router.UriRoute;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;

public interface RouteDataCollector<T> {
   Publisher<T> getData(Stream<UriRoute> routes);
}
