package io.micronaut.web.router.version;

import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.web.router.filter.RouteMatchFilter;

@DefaultImplementation(RouteVersionFilter.class)
public interface VersionRouteMatchFilter extends RouteMatchFilter {
}
