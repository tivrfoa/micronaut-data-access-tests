package io.micronaut.core.util.locale;

import io.micronaut.core.annotation.NonNull;
import java.util.Locale;
import java.util.Optional;

public interface LocaleResolutionConfiguration {
   @NonNull
   Optional<Locale> getFixed();

   @NonNull
   Locale getDefaultLocale();
}
