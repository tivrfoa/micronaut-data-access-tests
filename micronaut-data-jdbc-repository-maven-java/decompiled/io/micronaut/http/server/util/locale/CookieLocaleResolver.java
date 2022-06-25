package io.micronaut.http.server.util.locale;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import jakarta.inject.Singleton;
import java.util.Locale;
import java.util.Optional;

@Singleton
@Requires(
   property = "micronaut.server.locale-resolution.cookie-name"
)
public class CookieLocaleResolver extends HttpAbstractLocaleResolver {
   private final String cookieName;

   public CookieLocaleResolver(HttpLocaleResolutionConfiguration httpLocaleResolutionConfiguration) {
      super(httpLocaleResolutionConfiguration);
      this.cookieName = (String)httpLocaleResolutionConfiguration.getCookieName()
         .orElseThrow(() -> new IllegalArgumentException("The locale cookie name must be set"));
   }

   @NonNull
   public Optional<Locale> resolve(@NonNull HttpRequest<?> request) {
      return request.getCookies().get(this.cookieName, Locale.class);
   }
}
