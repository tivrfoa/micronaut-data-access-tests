package io.micronaut.web.router;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.FilterMatcher;
import io.micronaut.http.filter.FilterPatternStyle;
import io.micronaut.http.filter.HttpFilter;
import io.micronaut.http.filter.HttpFilterResolver;
import io.micronaut.http.filter.HttpServerFilterResolver;
import io.micronaut.http.uri.UriMatchTemplate;
import io.micronaut.web.router.exceptions.RoutingException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Singleton
public class DefaultRouter implements Router, HttpServerFilterResolver<RouteMatch<?>> {
   private final Map<String, List<UriRoute>> routesByMethod = new HashMap();
   private final List<StatusRoute> statusRoutes = new ArrayList();
   private final List<ErrorRoute> errorRoutes = new ArrayList();
   private final Set<Integer> exposedPorts;
   private final List<FilterRoute> alwaysMatchesFilterRoutes = new ArrayList();
   private final List<FilterRoute> preconditionFilterRoutes = new ArrayList();
   private final Supplier<List<HttpFilter>> alwaysMatchesHttpFilters = SupplierUtil.memoized(() -> {
      if (this.alwaysMatchesFilterRoutes.isEmpty()) {
         return Collections.emptyList();
      } else {
         List<HttpFilter> httpFilters = new ArrayList(this.alwaysMatchesFilterRoutes.size());

         for(FilterRoute filterRoutex : this.alwaysMatchesFilterRoutes) {
            httpFilters.add(filterRoutex.getFilter());
         }

         httpFilters.sort(OrderUtil.COMPARATOR);
         return httpFilters;
      }
   });

   public DefaultRouter(RouteBuilder... builders) {
      this(Arrays.asList(builders));
   }

   @Inject
   public DefaultRouter(Collection<RouteBuilder> builders) {
      Set<Integer> exposedPorts = new HashSet(5);
      List<FilterRoute> filterRoutes = new ArrayList();

      for(RouteBuilder builder : builders) {
         for(UriRoute route : builder.getUriRoutes()) {
            String key = route.getHttpMethodName();
            ((List)this.routesByMethod.computeIfAbsent(key, x -> new ArrayList())).add(route);
         }

         for(StatusRoute statusRoute : builder.getStatusRoutes()) {
            if (this.statusRoutes.contains(statusRoute)) {
               StatusRoute existing = (StatusRoute)this.statusRoutes.stream().filter(r -> r.equals(statusRoute)).findFirst().orElse(null);
               throw new RoutingException(
                  "Attempted to register multiple local routes for http status ["
                     + statusRoute.status()
                     + "]. New route: "
                     + statusRoute
                     + ". Existing: "
                     + existing
               );
            }

            this.statusRoutes.add(statusRoute);
         }

         for(ErrorRoute errorRoute : builder.getErrorRoutes()) {
            if (this.errorRoutes.contains(errorRoute)) {
               ErrorRoute existing = (ErrorRoute)this.errorRoutes.stream().filter(r -> r.equals(errorRoute)).findFirst().orElse(null);
               throw new RoutingException(
                  "Attempted to register multiple local routes for error ["
                     + errorRoute.exceptionType().getSimpleName()
                     + "]. New route: "
                     + errorRoute
                     + ". Existing: "
                     + existing
               );
            }

            this.errorRoutes.add(errorRoute);
         }

         filterRoutes.addAll(builder.getFilterRoutes());
         exposedPorts.addAll(builder.getExposedPorts());
      }

      if (CollectionUtils.isNotEmpty(exposedPorts)) {
         this.exposedPorts = exposedPorts;
      } else {
         this.exposedPorts = Collections.emptySet();
      }

      this.routesByMethod.values().forEach(this::finalizeRoutes);

      for(FilterRoute filterRoute : filterRoutes) {
         if (this.isMatchesAll(filterRoute)) {
            this.alwaysMatchesFilterRoutes.add(filterRoute);
         } else {
            this.preconditionFilterRoutes.add(filterRoute);
         }
      }

   }

   private boolean isMatchesAll(FilterRoute filterRoute) {
      if (filterRoute.getAnnotationMetadata().hasStereotype(FilterMatcher.NAME)) {
         return false;
      } else if (filterRoute.hasMethods()) {
         return false;
      } else {
         if (filterRoute.hasPatterns()) {
            for(String pattern : filterRoute.getPatterns()) {
               if (!"/**".equals(pattern)) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   @Override
   public Set<Integer> getExposedPorts() {
      return this.exposedPorts;
   }

   @Override
   public void applyDefaultPorts(List<Integer> ports) {
      Predicate<HttpRequest<?>> portMatches = httpRequest -> ports.contains(httpRequest.getServerAddress().getPort());

      for(List<UriRoute> routes : this.routesByMethod.values()) {
         for(int i = 0; i < routes.size(); ++i) {
            UriRoute route = (UriRoute)routes.get(i);
            if (route.getPort() == null) {
               routes.set(i, route.where(portMatches));
            }
         }
      }

   }

   @NonNull
   @Override
   public <T, R> Stream<UriRouteMatch<T, R>> find(@NonNull HttpRequest<?> request, @NonNull CharSequence uri) {
      return this.find(request.getMethodName(), uri, null).stream();
   }

   @NonNull
   @Override
   public <T, R> Stream<UriRouteMatch<T, R>> find(@NonNull HttpRequest<?> request) {
      boolean permitsBody = HttpMethod.permitsRequestBody(request.getMethod());
      return this.find(request, request.getPath())
         .filter(match -> match.test(request) && (!permitsBody || match.doesConsume((MediaType)request.getContentType().orElse(null))));
   }

   @NonNull
   @Override
   public <T, R> Stream<UriRouteMatch<T, R>> find(@NonNull HttpMethod httpMethod, @NonNull CharSequence uri, @Nullable HttpRequest<?> context) {
      return this.find(httpMethod.name(), uri, null).stream();
   }

   @NonNull
   @Override
   public Stream<UriRoute> uriRoutes() {
      return this.routesByMethod.values().stream().flatMap(Collection::stream);
   }

   @NonNull
   @Override
   public <T, R> List<UriRouteMatch<T, R>> findAllClosest(@NonNull HttpRequest<?> request) {
      HttpMethod httpMethod = request.getMethod();
      MediaType contentType = (MediaType)request.getContentType().orElse(null);
      boolean permitsBody = HttpMethod.permitsRequestBody(httpMethod);
      Collection<MediaType> acceptedProducedTypes = request.accept();
      List<UriRouteMatch<T, R>> uriRoutes = this.find(
         request.getMethodName(),
         request.getPath(),
         routeMatch -> routeMatch.test(request) && (!permitsBody || routeMatch.doesConsume(contentType)) && routeMatch.doesProduce(acceptedProducedTypes)
      );
      int routeCount = uriRoutes.size();
      if (routeCount <= 1) {
         return uriRoutes;
      } else {
         if (CollectionUtils.isNotEmpty(acceptedProducedTypes)) {
            MediaType mediaType = (MediaType)acceptedProducedTypes.iterator().next();
            List<UriRouteMatch<T, R>> mostSpecific = new ArrayList(uriRoutes.size());

            for(UriRouteMatch<T, R> routeMatch : uriRoutes) {
               if (routeMatch.explicitlyProduces(mediaType)) {
                  mostSpecific.add(routeMatch);
               }
            }

            if (!mostSpecific.isEmpty() || !acceptedProducedTypes.contains(MediaType.ALL_TYPE)) {
               uriRoutes = mostSpecific;
            }
         }

         routeCount = uriRoutes.size();
         if (routeCount > 1 && permitsBody) {
            List<UriRouteMatch<T, R>> explicitlyConsumedRoutes = new ArrayList(routeCount);
            List<UriRouteMatch<T, R>> consumesRoutes = new ArrayList(routeCount);

            for(UriRouteMatch<T, R> match : uriRoutes) {
               if (match.explicitlyConsumes(contentType != null ? contentType : MediaType.ALL_TYPE)) {
                  explicitlyConsumedRoutes.add(match);
               }

               if (explicitlyConsumedRoutes.isEmpty() && match.doesConsume(contentType)) {
                  consumesRoutes.add(match);
               }
            }

            uriRoutes = explicitlyConsumedRoutes.isEmpty() ? consumesRoutes : explicitlyConsumedRoutes;
         }

         routeCount = uriRoutes.size();
         if (routeCount > 1) {
            long variableCount = 0L;
            long rawLength = 0L;
            List<UriRouteMatch<T, R>> closestMatches = new ArrayList(routeCount);

            for(int i = 0; i < routeCount; ++i) {
               UriRouteMatch<T, R> match = (UriRouteMatch)uriRoutes.get(i);
               UriMatchTemplate template = match.getRoute().getUriMatchTemplate();
               long variable = template.getPathVariableSegmentCount();
               long raw = (long)template.getRawSegmentLength();
               if (i == 0) {
                  variableCount = variable;
                  rawLength = raw;
               }

               if (variable > variableCount || raw < rawLength) {
                  break;
               }

               closestMatches.add(match);
            }

            uriRoutes = closestMatches;
         }

         return uriRoutes;
      }
   }

   @NonNull
   @Override
   public <T, R> Optional<UriRouteMatch<T, R>> route(@NonNull HttpMethod httpMethod, @NonNull CharSequence uri) {
      for(UriRoute uriRoute : (List)this.routesByMethod.getOrDefault(httpMethod.name(), Collections.emptyList())) {
         Optional<UriRouteMatch> match = uriRoute.match(uri.toString());
         if (match.isPresent()) {
            return match;
         }
      }

      return Optional.empty();
   }

   @Override
   public <R> Optional<RouteMatch<R>> route(@NonNull HttpStatus status) {
      for(StatusRoute statusRoute : this.statusRoutes) {
         if (statusRoute.originatingType() == null) {
            Optional<RouteMatch<R>> match = statusRoute.match(status);
            if (match.isPresent()) {
               return match;
            }
         }
      }

      return Optional.empty();
   }

   @Override
   public <R> Optional<RouteMatch<R>> route(@NonNull Class originatingClass, @NonNull HttpStatus status) {
      for(StatusRoute statusRoute : this.statusRoutes) {
         Optional<RouteMatch<R>> match = statusRoute.match(originatingClass, status);
         if (match.isPresent()) {
            return match;
         }
      }

      return Optional.empty();
   }

   @Override
   public <R> Optional<RouteMatch<R>> route(@NonNull Class originatingClass, @NonNull Throwable error) {
      Map<ErrorRoute, RouteMatch<R>> matchedRoutes = new LinkedHashMap();

      for(ErrorRoute errorRoute : this.errorRoutes) {
         Optional<RouteMatch<R>> match = errorRoute.match(originatingClass, error);
         match.ifPresent(m -> {
            RouteMatch var10000 = (RouteMatch)matchedRoutes.put(errorRoute, m);
         });
      }

      return this.findRouteMatch(matchedRoutes, error);
   }

   @Override
   public <R> Optional<RouteMatch<R>> findErrorRoute(@NonNull Class<?> originatingClass, @NonNull Throwable error, HttpRequest<?> request) {
      return this.findErrorRouteInternal(originatingClass, error, request);
   }

   private <R> Optional<RouteMatch<R>> findErrorRouteInternal(@Nullable Class<?> originatingClass, @NonNull Throwable error, HttpRequest<?> request) {
      Collection<MediaType> accept = request.accept();
      boolean hasAcceptHeader = CollectionUtils.isNotEmpty(accept);
      if (hasAcceptHeader) {
         Map<ErrorRoute, RouteMatch<R>> matchedRoutes = new LinkedHashMap();

         for(ErrorRoute errorRoute : this.errorRoutes) {
            RouteMatch<R> match = (RouteMatch)errorRoute.match(originatingClass, error).orElse(null);
            if (match != null && match.doesProduce(accept)) {
               matchedRoutes.put(errorRoute, match);
            }
         }

         return this.findRouteMatch(matchedRoutes, error);
      } else {
         Map<ErrorRoute, RouteMatch<R>> producesAllMatchedRoutes = new LinkedHashMap();
         Map<ErrorRoute, RouteMatch<R>> producesSpecificMatchedRoutes = new LinkedHashMap();

         for(ErrorRoute errorRoute : this.errorRoutes) {
            RouteMatch<R> match = (RouteMatch)errorRoute.match(originatingClass, error).orElse(null);
            if (match != null) {
               List<MediaType> produces = match.getProduces();
               if (!CollectionUtils.isEmpty(produces) && !produces.contains(MediaType.ALL_TYPE)) {
                  producesSpecificMatchedRoutes.put(errorRoute, match);
               } else {
                  producesAllMatchedRoutes.put(errorRoute, match);
               }
            }
         }

         return producesAllMatchedRoutes.isEmpty()
            ? this.findRouteMatch(producesSpecificMatchedRoutes, error)
            : this.findRouteMatch(producesAllMatchedRoutes, error);
      }
   }

   @Override
   public <R> Optional<RouteMatch<R>> findErrorRoute(@NonNull Throwable error, HttpRequest<?> request) {
      return this.findErrorRouteInternal(null, error, request);
   }

   @Override
   public <R> Optional<RouteMatch<R>> findStatusRoute(@NonNull Class<?> originatingClass, @NonNull HttpStatus status, HttpRequest<?> request) {
      return this.findStatusInternal(originatingClass, status, request);
   }

   @Override
   public <R> Optional<RouteMatch<R>> findStatusRoute(@NonNull HttpStatus status, HttpRequest<?> request) {
      return this.findStatusInternal(null, status, request);
   }

   private <R> Optional<RouteMatch<R>> findStatusInternal(@Nullable Class<?> originatingClass, @NonNull HttpStatus status, HttpRequest<?> request) {
      Collection<MediaType> accept = request.accept();
      boolean hasAcceptHeader = CollectionUtils.isNotEmpty(accept);
      if (hasAcceptHeader) {
         for(StatusRoute statusRoute : this.statusRoutes) {
            RouteMatch<R> match = (RouteMatch)statusRoute.match(originatingClass, status).orElse(null);
            if (match != null && match.doesProduce(accept)) {
               return Optional.of(match);
            }
         }

         return Optional.empty();
      } else {
         RouteMatch<R> firstMatch = null;

         for(StatusRoute errorRoute : this.statusRoutes) {
            RouteMatch<R> match = (RouteMatch)errorRoute.match(originatingClass, status).orElse(null);
            if (match != null) {
               List<MediaType> produces = match.getProduces();
               if (CollectionUtils.isEmpty(produces) || produces.contains(MediaType.ALL_TYPE)) {
                  return Optional.of(match);
               }

               if (firstMatch == null) {
                  firstMatch = match;
               }
            }
         }

         return Optional.ofNullable(firstMatch);
      }
   }

   @Override
   public <R> Optional<RouteMatch<R>> route(@NonNull Throwable error) {
      Map<ErrorRoute, RouteMatch<R>> matchedRoutes = new LinkedHashMap();

      for(ErrorRoute errorRoute : this.errorRoutes) {
         if (errorRoute.originatingType() == null) {
            Optional<RouteMatch<R>> match = errorRoute.match(error);
            match.ifPresent(m -> {
               RouteMatch var10000 = (RouteMatch)matchedRoutes.put(errorRoute, m);
            });
         }
      }

      return this.findRouteMatch(matchedRoutes, error);
   }

   @NonNull
   @Override
   public List<HttpFilter> findFilters(@NonNull HttpRequest<?> request) {
      if (this.preconditionFilterRoutes.isEmpty()) {
         return (List<HttpFilter>)this.alwaysMatchesHttpFilters.get();
      } else {
         List<HttpFilter> httpFilters = new ArrayList(this.alwaysMatchesFilterRoutes.size() + this.preconditionFilterRoutes.size());
         httpFilters.addAll((Collection)this.alwaysMatchesHttpFilters.get());
         RouteMatch routeMatch = (RouteMatch)request.getAttribute(HttpAttributes.ROUTE_MATCH).filter(o -> o instanceof RouteMatch).orElse(null);
         HttpMethod method = request.getMethod();
         URI uri = request.getUri();

         for(FilterRoute filterRoute : this.preconditionFilterRoutes) {
            if (routeMatch == null || this.matchesFilterMatcher(filterRoute, routeMatch)) {
               filterRoute.match(method, uri).ifPresent(httpFilters::add);
            }
         }

         httpFilters.sort(OrderUtil.COMPARATOR);
         return Collections.unmodifiableList(httpFilters);
      }
   }

   @NonNull
   @Override
   public <T, R> Stream<UriRouteMatch<T, R>> findAny(@NonNull CharSequence uri, @Nullable HttpRequest<?> context) {
      List matchedRoutes = new ArrayList(5);
      String uriStr = uri.toString();

      for(List<UriRoute> routes : this.routesByMethod.values()) {
         for(UriRoute route : routes) {
            UriRouteMatch match = (UriRouteMatch)route.match(uriStr).orElse(null);
            if (match != null && match.test(context)) {
               matchedRoutes.add(match);
            }
         }
      }

      return matchedRoutes.stream();
   }

   private <T, R> List<UriRouteMatch<T, R>> find(String httpMethodName, CharSequence uri, @Nullable Predicate<UriRouteMatch> predicate) {
      List<UriRoute> routes = (List)this.routesByMethod.getOrDefault(httpMethodName, Collections.emptyList());
      if (CollectionUtils.isNotEmpty(routes)) {
         String uriStr = uri.toString();
         List<UriRouteMatch<T, R>> routeMatches = new LinkedList();

         for(UriRoute route : routes) {
            Optional<UriRouteMatch> match = route.match(uriStr);
            if (predicate != null) {
               match = match.filter(predicate);
            }

            match.ifPresent(routeMatches::add);
         }

         return routeMatches;
      } else {
         return Collections.emptyList();
      }
   }

   private UriRoute[] finalizeRoutes(List<UriRoute> routes) {
      Collections.sort(routes);
      return (UriRoute[])routes.toArray(new UriRoute[0]);
   }

   private <T> Optional<RouteMatch<T>> findRouteMatch(Map<ErrorRoute, RouteMatch<T>> matchedRoutes, Throwable error) {
      if (matchedRoutes.size() == 1) {
         return matchedRoutes.values().stream().findFirst();
      } else if (matchedRoutes.size() <= 1) {
         return Optional.empty();
      } else {
         int minCount = Integer.MAX_VALUE;
         Supplier<List<Class>> hierarchySupplier = () -> ClassUtils.resolveHierarchy(error.getClass());
         Optional<RouteMatch<T>> match = Optional.empty();
         Class errorClass = error.getClass();

         for(Entry<ErrorRoute, RouteMatch<T>> entry : matchedRoutes.entrySet()) {
            Class exceptionType = ((ErrorRoute)entry.getKey()).exceptionType();
            if (exceptionType.equals(errorClass)) {
               match = Optional.of(entry.getValue());
               break;
            }

            List<Class> hierarchy = (List)hierarchySupplier.get();
            int index = hierarchy.indexOf(exceptionType);
            if (index > -1 && index < minCount) {
               minCount = index;
               match = Optional.of(entry.getValue());
            }
         }

         return match;
      }
   }

   public List<HttpFilterResolver.FilterEntry<HttpFilter>> resolveFilterEntries(RouteMatch<?> routeMatch) {
      if (this.preconditionFilterRoutes.isEmpty()) {
         return this.alwaysMatchesFilterRoutes;
      } else {
         List<HttpFilterResolver.FilterEntry<HttpFilter>> filterEntries = new ArrayList(
            this.alwaysMatchesFilterRoutes.size() + this.preconditionFilterRoutes.size()
         );
         filterEntries.addAll(this.alwaysMatchesFilterRoutes);

         for(FilterRoute filterRoute : this.preconditionFilterRoutes) {
            if (!this.matchesFilterMatcher(filterRoute, routeMatch)) {
               filterEntries.add(filterRoute);
            }
         }

         filterEntries.sort(OrderUtil.COMPARATOR);
         return Collections.unmodifiableList(filterEntries);
      }
   }

   @Override
   public List<HttpFilter> resolveFilters(HttpRequest<?> request, List<HttpFilterResolver.FilterEntry<HttpFilter>> filterEntries) {
      List<HttpFilter> httpFilters = new ArrayList(filterEntries.size());
      Iterator var4 = filterEntries.iterator();

      while(true) {
         HttpFilterResolver.FilterEntry<HttpFilter> entry;
         while(true) {
            if (!var4.hasNext()) {
               httpFilters.sort(OrderUtil.COMPARATOR);
               return Collections.unmodifiableList(httpFilters);
            }

            entry = (HttpFilterResolver.FilterEntry)var4.next();
            if (!entry.hasMethods() || entry.getFilterMethods().contains(request.getMethod())) {
               if (!entry.hasPatterns()) {
                  break;
               }

               String path = request.getPath();
               String[] patterns = entry.getPatterns();
               FilterPatternStyle patternStyle = (FilterPatternStyle)entry.getAnnotationMetadata()
                  .enumValue("patternStyle", FilterPatternStyle.class)
                  .orElse(FilterPatternStyle.ANT);
               boolean matches = true;

               for(String pattern : patterns) {
                  if (!matches) {
                     break;
                  }

                  matches = "/**".equals(pattern) || patternStyle.getPathMatcher().matches(pattern, path);
               }

               if (matches) {
                  break;
               }
            }
         }

         httpFilters.add(entry.getFilter());
      }
   }

   private boolean matchesFilterMatcher(FilterRoute filterRoute, RouteMatch<?> context) {
      AnnotationMetadata annotationMetadata = filterRoute.getAnnotationMetadata();
      boolean matches = !annotationMetadata.hasStereotype(FilterMatcher.NAME);
      if (!matches) {
         String filterAnnotation = (String)annotationMetadata.getAnnotationNameByStereotype(FilterMatcher.NAME).orElse(null);
         if (filterAnnotation != null) {
            matches = context.getAnnotationMetadata().hasStereotype(filterAnnotation);
         }
      }

      return matches;
   }
}
