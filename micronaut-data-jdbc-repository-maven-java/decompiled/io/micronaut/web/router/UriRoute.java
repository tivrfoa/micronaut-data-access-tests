package io.micronaut.web.router;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.uri.UriMatchTemplate;
import io.micronaut.http.uri.UriMatcher;
import java.net.URI;
import java.util.Optional;
import java.util.function.Predicate;

public interface UriRoute extends Route, UriMatcher, Comparable<UriRoute> {
   UriRoute nest(Runnable nested);

   HttpMethod getHttpMethod();

   UriMatchTemplate getUriMatchTemplate();

   @Override
   default Optional<UriRouteMatch> match(URI uri) {
      return this.match(uri.toString());
   }

   @Override
   Optional<UriRouteMatch> match(String uri);

   UriRoute consumes(MediaType... mediaType);

   UriRoute produces(MediaType... mediaType);

   UriRoute consumesAll();

   UriRoute where(Predicate<HttpRequest<?>> condition);

   UriRoute body(String argument);

   UriRoute exposedPort(int port);

   @Nullable
   Integer getPort();

   default String getHttpMethodName() {
      return this.getHttpMethod().name();
   }
}
