package io.micronaut.web.router.filter;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.filter.HttpFilter;
import io.micronaut.web.router.RouteMatch;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.UriRoute;
import io.micronaut.web.router.UriRouteMatch;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilteredRouter implements Router {
   private final Router router;
   private final RouteMatchFilter routeFilter;

   public FilteredRouter(Router router, RouteMatchFilter routeFilter) {
      this.router = router;
      this.routeFilter = routeFilter;
   }

   @NonNull
   @Override
   public <T, R> Stream<UriRouteMatch<T, R>> findAny(@NonNull CharSequence uri, @Nullable HttpRequest<?> context) {
      Stream<UriRouteMatch<T, R>> matchStream = this.router.findAny(uri, context);
      return context != null ? matchStream.filter(this.routeFilter.filter(context)) : matchStream;
   }

   @Override
   public Set<Integer> getExposedPorts() {
      return this.router.getExposedPorts();
   }

   @Override
   public void applyDefaultPorts(List<Integer> ports) {
      this.router.applyDefaultPorts(ports);
   }

   @NonNull
   @Override
   public <T, R> Stream<UriRouteMatch<T, R>> find(@NonNull HttpMethod httpMethod, @NonNull CharSequence uri, @Nullable HttpRequest<?> context) {
      Stream<UriRouteMatch<T, R>> matchStream = this.router.find(httpMethod, uri, context);
      return context != null ? matchStream.filter(this.routeFilter.filter(context)) : matchStream;
   }

   @NonNull
   @Override
   public <T, R> List<UriRouteMatch<T, R>> findAllClosest(@NonNull HttpRequest<?> request) {
      List<UriRouteMatch<T, R>> closestMatches = this.router.findAllClosest(request);
      return (List<UriRouteMatch<T, R>>)closestMatches.stream().filter(this.routeFilter.filter(request)).collect(Collectors.toList());
   }

   @NonNull
   @Override
   public <T, R> Stream<UriRouteMatch<T, R>> find(@NonNull HttpRequest<?> request, @NonNull CharSequence uri) {
      return this.router.find(request, uri);
   }

   @NonNull
   @Override
   public Stream<UriRoute> uriRoutes() {
      return this.router.uriRoutes();
   }

   @Override
   public <T, R> Optional<UriRouteMatch<T, R>> route(@NonNull HttpMethod httpMethod, @NonNull CharSequence uri) {
      return this.router.route(httpMethod, uri);
   }

   @Override
   public <R> Optional<RouteMatch<R>> route(@NonNull HttpStatus status) {
      return this.router.route(status);
   }

   @Override
   public <R> Optional<RouteMatch<R>> route(@NonNull Class originatingClass, @NonNull HttpStatus status) {
      return this.router.route(originatingClass, status);
   }

   @Override
   public <R> Optional<RouteMatch<R>> route(@NonNull Throwable error) {
      return this.router.route(error);
   }

   @Override
   public <R> Optional<RouteMatch<R>> route(@NonNull Class originatingClass, @NonNull Throwable error) {
      return this.router.route(originatingClass, error);
   }

   @Override
   public <R> Optional<RouteMatch<R>> findErrorRoute(@NonNull Class<?> originatingClass, @NonNull Throwable error, HttpRequest<?> request) {
      return this.router.findErrorRoute(originatingClass, error, request);
   }

   @Override
   public <R> Optional<RouteMatch<R>> findErrorRoute(@NonNull Throwable error, HttpRequest<?> request) {
      return this.router.findErrorRoute(error, request);
   }

   @Override
   public <R> Optional<RouteMatch<R>> findStatusRoute(@NonNull Class<?> originatingClass, @NonNull HttpStatus status, HttpRequest<?> request) {
      return this.router.findStatusRoute(originatingClass, status, request);
   }

   @Override
   public <R> Optional<RouteMatch<R>> findStatusRoute(@NonNull HttpStatus status, HttpRequest<?> request) {
      return this.router.findStatusRoute(status, request);
   }

   @NonNull
   @Override
   public List<HttpFilter> findFilters(@NonNull HttpRequest<?> request) {
      return this.router.findFilters(request);
   }

   @Override
   public <T, R> Optional<UriRouteMatch<T, R>> GET(@NonNull CharSequence uri) {
      return this.router.GET(uri);
   }

   @Override
   public <T, R> Optional<UriRouteMatch<T, R>> POST(@NonNull CharSequence uri) {
      return this.router.POST(uri);
   }

   @Override
   public <T, R> Optional<UriRouteMatch<T, R>> PUT(@NonNull CharSequence uri) {
      return this.router.PUT(uri);
   }

   @Override
   public <T, R> Optional<UriRouteMatch<T, R>> PATCH(@NonNull CharSequence uri) {
      return this.router.PATCH(uri);
   }

   @Override
   public <T, R> Optional<UriRouteMatch<T, R>> DELETE(@NonNull CharSequence uri) {
      return this.router.DELETE(uri);
   }

   @Override
   public <T, R> Optional<UriRouteMatch<T, R>> OPTIONS(@NonNull CharSequence uri) {
      return this.router.OPTIONS(uri);
   }

   @Override
   public <T, R> Optional<UriRouteMatch<T, R>> HEAD(@NonNull CharSequence uri) {
      return this.router.HEAD(uri);
   }

   @NonNull
   @Override
   public <T, R> Stream<UriRouteMatch<T, R>> find(@NonNull HttpRequest<?> request) {
      Stream<UriRouteMatch<T, R>> matches = this.router.find(request);
      return matches.filter(this.routeFilter.filter(request));
   }
}
