package io.micronaut.web.router;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.filter.HttpFilter;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface Router {
   @NonNull
   <T, R> Stream<UriRouteMatch<T, R>> findAny(@NonNull CharSequence uri, @Nullable HttpRequest<?> context);

   Set<Integer> getExposedPorts();

   void applyDefaultPorts(List<Integer> ports);

   @NonNull
   <T, R> Stream<UriRouteMatch<T, R>> find(@NonNull HttpMethod httpMethod, @NonNull CharSequence uri, @Nullable HttpRequest<?> context);

   @NonNull
   default <T, R> Stream<UriRouteMatch<T, R>> find(@NonNull HttpMethod httpMethod, @NonNull URI uri, @Nullable HttpRequest<?> context) {
      return this.find(httpMethod, uri.toString(), context);
   }

   @NonNull
   default <T, R> Stream<UriRouteMatch<T, R>> find(@NonNull HttpRequest<?> request) {
      return this.find(request, request.getPath());
   }

   @NonNull
   default <T, R> Stream<UriRouteMatch<T, R>> find(@NonNull HttpRequest<?> request, @NonNull CharSequence uri) {
      return this.find(HttpMethod.valueOf(request.getMethodName()), uri, request);
   }

   @NonNull
   <T, R> List<UriRouteMatch<T, R>> findAllClosest(@NonNull HttpRequest<?> request);

   @NonNull
   Stream<UriRoute> uriRoutes();

   <T, R> Optional<UriRouteMatch<T, R>> route(@NonNull HttpMethod httpMethod, @NonNull CharSequence uri);

   <R> Optional<RouteMatch<R>> route(@NonNull HttpStatus status);

   <R> Optional<RouteMatch<R>> route(@NonNull Class originatingClass, @NonNull HttpStatus status);

   <R> Optional<RouteMatch<R>> route(@NonNull Throwable error);

   <R> Optional<RouteMatch<R>> route(@NonNull Class originatingClass, @NonNull Throwable error);

   <R> Optional<RouteMatch<R>> findErrorRoute(@NonNull Class<?> originatingClass, @NonNull Throwable error, HttpRequest<?> request);

   <R> Optional<RouteMatch<R>> findErrorRoute(@NonNull Throwable error, HttpRequest<?> request);

   <R> Optional<RouteMatch<R>> findStatusRoute(@NonNull Class<?> originatingClass, @NonNull HttpStatus status, HttpRequest<?> request);

   <R> Optional<RouteMatch<R>> findStatusRoute(@NonNull HttpStatus status, HttpRequest<?> request);

   @NonNull
   List<HttpFilter> findFilters(@NonNull HttpRequest<?> request);

   default <T, R> Optional<UriRouteMatch<T, R>> GET(@NonNull CharSequence uri) {
      return this.route(HttpMethod.GET, uri);
   }

   default <T, R> Optional<UriRouteMatch<T, R>> POST(@NonNull CharSequence uri) {
      return this.route(HttpMethod.POST, uri);
   }

   default <T, R> Optional<UriRouteMatch<T, R>> PUT(@NonNull CharSequence uri) {
      return this.route(HttpMethod.PUT, uri);
   }

   default <T, R> Optional<UriRouteMatch<T, R>> PATCH(@NonNull CharSequence uri) {
      return this.route(HttpMethod.PATCH, uri);
   }

   default <T, R> Optional<UriRouteMatch<T, R>> DELETE(@NonNull CharSequence uri) {
      return this.route(HttpMethod.DELETE, uri);
   }

   default <T, R> Optional<UriRouteMatch<T, R>> OPTIONS(@NonNull CharSequence uri) {
      return this.route(HttpMethod.OPTIONS, uri);
   }

   default <T, R> Optional<UriRouteMatch<T, R>> HEAD(@NonNull CharSequence uri) {
      return this.route(HttpMethod.HEAD, uri);
   }
}
