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
   beans = {RoutesVersioningConfiguration.class, ParameterVersionResolverConfiguration.class}
)
public class ParameterVersionResolver implements RequestVersionResolver {
   private final List<String> parameterNames;

   public ParameterVersionResolver(ParameterVersionResolverConfiguration configuration) {
      this.parameterNames = configuration.getNames();
   }

   public Optional<String> resolve(HttpRequest<?> request) {
      return this.parameterNames.stream().map(name -> request.getParameters().get(name)).filter(Objects::nonNull).findFirst();
   }
}
