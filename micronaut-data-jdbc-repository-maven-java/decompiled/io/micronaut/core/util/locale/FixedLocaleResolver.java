package io.micronaut.core.util.locale;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.LocaleResolver;
import java.util.Locale;
import java.util.Optional;

public class FixedLocaleResolver<T> implements LocaleResolver<T> {
   protected final Locale locale;

   public FixedLocaleResolver(Locale locale) {
      this.locale = locale;
   }

   @NonNull
   @Override
   public Optional<Locale> resolve(@NonNull T context) {
      return Optional.of(this.locale);
   }

   @NonNull
   @Override
   public Locale resolveOrDefault(@NonNull T context) {
      return (Locale)this.resolve(context).orElseThrow(() -> new IllegalArgumentException("The fixed locale must be set"));
   }
}
