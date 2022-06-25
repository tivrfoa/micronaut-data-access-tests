package io.micronaut.web.router;

import io.micronaut.http.HttpMethod;
import io.micronaut.http.filter.FilterPatternStyle;
import io.micronaut.http.filter.HttpFilter;
import io.micronaut.http.filter.HttpFilterResolver;
import java.net.URI;
import java.util.Optional;

public interface FilterRoute extends HttpFilterResolver.FilterEntry<HttpFilter> {
   HttpFilter getFilter();

   Optional<HttpFilter> match(HttpMethod method, URI uri);

   FilterRoute pattern(String pattern);

   FilterRoute methods(HttpMethod... methods);

   FilterRoute patternStyle(FilterPatternStyle patternStyle);
}
