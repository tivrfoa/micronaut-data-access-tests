package io.micronaut.web.router.version.resolution;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.web.router.version.RoutesVersioningConfiguration;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Singleton
@Requires(
   beans = {RoutesVersioningConfiguration.class, HeaderVersionResolverConfiguration.class}
)
public class HeaderVersionResolver implements RequestVersionResolver {
   private final List<String> headerNames;

   public HeaderVersionResolver(HeaderVersionResolverConfiguration configuration) {
      this.headerNames = configuration.getNames();
   }

   public Optional<String> resolve(HttpRequest<?> request) {
      return this.headerNames.stream().map(name -> request.getHeaders().get(name)).filter(Objects::nonNull).findFirst();
   }
}
