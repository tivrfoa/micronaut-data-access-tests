package io.micronaut.web.router.resource;

import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import java.util.List;

@Factory
public class StaticResourceResolverFactory {
   @Singleton
   @NonNull
   protected StaticResourceResolver build(List<StaticResourceConfiguration> configurations) {
      return configurations.isEmpty() ? StaticResourceResolver.EMPTY : new StaticResourceResolver(configurations);
   }
}
