package io.micronaut.http.cookie;

import io.micronaut.core.annotation.NonNull;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

public interface CookieConfiguration {
   @NonNull
   String getCookieName();

   Optional<String> getCookieDomain();

   Optional<String> getCookiePath();

   Optional<Boolean> isCookieHttpOnly();

   Optional<Boolean> isCookieSecure();

   Optional<TemporalAmount> getCookieMaxAge();

   default Optional<SameSite> getCookieSameSite() {
      return Optional.empty();
   }
}
