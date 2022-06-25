package io.micronaut.management.endpoint.routes;

import io.micronaut.web.router.UriRoute;

public interface RouteData<T> {
   T getData(UriRoute route);
}
