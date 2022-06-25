package io.micronaut.http.server.util.locale;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import jakarta.inject.Singleton;
import java.util.Locale;
import java.util.Optional;

@Singleton
@Requires(
   property = "micronaut.server.locale-resolution.header",
   notEquals = "false"
)
public class RequestLocaleResolver extends HttpAbstractLocaleResolver {
   public static final Integer ORDER = HttpAbstractLocaleResolver.ORDER + 25;

   public RequestLocaleResolver(HttpLocaleResolutionConfiguration httpLocaleResolutionConfiguration) {
      super(httpLocaleResolutionConfiguration);
   }

   @NonNull
   public Optional<Locale> resolve(@NonNull HttpRequest<?> request) {
      return request.getLocale();
   }

   @Override
   public int getOrder() {
      return ORDER;
   }
}
