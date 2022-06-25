package io.micronaut.http.server.util.locale;

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

@Singleton
@Primary
public class CompositeHttpLocaleResolver extends HttpAbstractLocaleResolver {
   private final HttpLocaleResolver[] localeResolvers;

   public CompositeHttpLocaleResolver(HttpLocaleResolver[] localeResolvers, HttpLocaleResolutionConfiguration httpLocaleResolutionConfiguration) {
      super(httpLocaleResolutionConfiguration);
      this.localeResolvers = localeResolvers;
   }

   @NonNull
   public Optional<Locale> resolve(@NonNull HttpRequest<?> request) {
      return (Optional<Locale>)Arrays.stream(this.localeResolvers)
         .map(resolver -> resolver.resolve(request))
         .filter(Optional::isPresent)
         .findFirst()
         .orElse(Optional.empty());
   }
}
