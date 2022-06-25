package io.micronaut.http.cookie;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

public interface Cookie extends Comparable<Cookie>, Serializable {
   @NonNull
   String getName();

   @NonNull
   String getValue();

   @Nullable
   String getDomain();

   @Nullable
   String getPath();

   boolean isHttpOnly();

   boolean isSecure();

   long getMaxAge();

   default Optional<SameSite> getSameSite() {
      return Optional.empty();
   }

   @NonNull
   default Cookie sameSite(@Nullable SameSite sameSite) {
      return this;
   }

   @NonNull
   Cookie maxAge(long maxAge);

   @NonNull
   Cookie value(@NonNull String value);

   @NonNull
   Cookie domain(@Nullable String domain);

   @NonNull
   Cookie path(@Nullable String path);

   @NonNull
   Cookie secure(boolean secure);

   @NonNull
   Cookie httpOnly(boolean httpOnly);

   @NonNull
   default Cookie configure(@NonNull CookieConfiguration configuration) {
      ArgumentUtils.requireNonNull("configuration", configuration);
      return this.configure(configuration, true);
   }

   @NonNull
   default Cookie configure(@NonNull CookieConfiguration configuration, boolean isSecure) {
      ArgumentUtils.requireNonNull("configuration", configuration);
      configuration.getCookiePath().ifPresent(this::path);
      configuration.getCookieDomain().ifPresent(this::domain);
      configuration.getCookieMaxAge().ifPresent(this::maxAge);
      configuration.isCookieHttpOnly().ifPresent(this::httpOnly);
      Optional<Boolean> secureConfiguration = configuration.isCookieSecure();
      if (secureConfiguration.isPresent()) {
         this.secure(secureConfiguration.get());
      } else {
         this.secure(isSecure);
      }

      configuration.getCookieSameSite().ifPresent(this::sameSite);
      return this;
   }

   @NonNull
   default Cookie maxAge(@NonNull TemporalAmount maxAge) {
      ArgumentUtils.requireNonNull("maxAge", maxAge);
      return this.maxAge(maxAge.get(ChronoUnit.SECONDS));
   }

   @NonNull
   static Cookie of(@NonNull String name, @NonNull String value) {
      CookieFactory instance = CookieFactory.INSTANCE;
      if (instance != null) {
         return instance.create(name, value);
      } else {
         throw new UnsupportedOperationException("No CookeFactory implementation found. Server implementation does not support cookies.");
      }
   }
}
