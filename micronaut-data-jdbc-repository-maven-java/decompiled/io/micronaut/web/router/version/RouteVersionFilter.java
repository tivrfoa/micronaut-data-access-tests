package io.micronaut.web.router.version;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.version.annotation.Version;
import io.micronaut.http.HttpRequest;
import io.micronaut.web.router.UriRouteMatch;
import io.micronaut.web.router.version.resolution.RequestVersionResolver;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(
   beans = {RoutesVersioningConfiguration.class}
)
public class RouteVersionFilter implements VersionRouteMatchFilter {
   private static final Logger LOG = LoggerFactory.getLogger(RouteVersionFilter.class);
   private final List<RequestVersionResolver> resolvingStrategies;
   private final DefaultVersionProvider defaultVersionProvider;

   public RouteVersionFilter(List<RequestVersionResolver> resolvingStrategies, @Nullable DefaultVersionProvider defaultVersionProvider) {
      this.resolvingStrategies = resolvingStrategies;
      this.defaultVersionProvider = defaultVersionProvider;
   }

   @Override
   public <T, R> Predicate<UriRouteMatch<T, R>> filter(HttpRequest<?> request) {
      ArgumentUtils.requireNonNull("request", request);
      if (this.resolvingStrategies != null && !this.resolvingStrategies.isEmpty()) {
         Optional<String> defaultVersion = this.defaultVersionProvider == null
            ? Optional.empty()
            : Optional.of(this.defaultVersionProvider.resolveDefaultVersion());
         Optional<String> version = this.resolveVersion(request);
         return match -> {
            Optional<String> routeVersion = this.getVersion(match);
            return routeVersion.isPresent()
               ? this.matchIfRouteIsVersioned(request, (String)version.orElse(defaultVersion.orElse(null)), (String)routeVersion.get())
               : this.matchIfRouteIsNotVersioned(request, (String)version.orElse(null));
         };
      } else {
         return match -> true;
      }
   }

   protected boolean matchIfRouteIsNotVersioned(@NonNull HttpRequest<?> request, @Nullable String version) {
      if (version != null) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Route does not specify a version but the version {} was resolved for request to URI {}", version, request.getUri());
         }

         return false;
      } else {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Route does not specify a version and no version was resolved for request to URI {}", request.getUri());
         }

         return true;
      }
   }

   protected boolean matchIfRouteIsVersioned(@NonNull HttpRequest<?> request, @Nullable String resolvedVersion, @NonNull String routeVersion) {
      if (resolvedVersion == null) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Route specifies a version {} and no version information resolved for request to URI {}", routeVersion, request.getUri());
         }

         return true;
      } else {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Route specifies a version {} and the version {} was resolved for request to URI {}", routeVersion, resolvedVersion, request.getUri());
         }

         return resolvedVersion.equals(routeVersion);
      }
   }

   @NonNull
   protected Optional<String> resolveVersion(@NonNull HttpRequest<?> request) {
      return this.resolvingStrategies.stream().map(strategy -> (String)strategy.resolve(request).orElse(null)).filter(Objects::nonNull).findFirst();
   }

   protected <T, R> Optional<String> getVersion(UriRouteMatch<T, R> routeMatch) {
      return routeMatch.getExecutableMethod().stringValue(Version.class);
   }
}
