package io.micronaut.web.router.filter;

import io.micronaut.http.HttpRequest;
import io.micronaut.web.router.UriRouteMatch;
import java.util.function.Predicate;

public interface RouteMatchFilter {
   <T, R> Predicate<UriRouteMatch<T, R>> filter(HttpRequest<?> request);
}
