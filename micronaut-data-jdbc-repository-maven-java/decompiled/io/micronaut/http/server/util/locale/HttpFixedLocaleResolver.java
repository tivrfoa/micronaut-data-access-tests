package io.micronaut.http.server.util.locale;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.locale.FixedLocaleResolver;
import io.micronaut.http.HttpRequest;
import jakarta.inject.Singleton;
import java.util.Locale;

@Singleton
@Requires(
   property = "micronaut.server.locale-resolution.fixed"
)
public class HttpFixedLocaleResolver extends FixedLocaleResolver<HttpRequest<?>> implements HttpLocaleResolver {
   public static final Integer ORDER = -2147483548;

   public HttpFixedLocaleResolver(HttpLocaleResolutionConfiguration localeResolutionConfiguration) {
      super((Locale)localeResolutionConfiguration.getFixed().orElseThrow(() -> new IllegalArgumentException("The fixed locale must be set")));
   }

   @Override
   public int getOrder() {
      return ORDER;
   }
}
