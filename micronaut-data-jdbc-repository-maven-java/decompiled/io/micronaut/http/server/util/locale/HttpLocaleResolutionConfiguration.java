package io.micronaut.http.server.util.locale;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.locale.LocaleResolutionConfiguration;
import java.util.Optional;

public interface HttpLocaleResolutionConfiguration extends LocaleResolutionConfiguration {
   @NonNull
   Optional<String> getSessionAttribute();

   @NonNull
   Optional<String> getCookieName();

   boolean isHeader();
}
