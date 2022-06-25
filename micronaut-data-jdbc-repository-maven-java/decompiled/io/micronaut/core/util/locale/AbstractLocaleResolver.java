package io.micronaut.core.util.locale;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.LocaleResolver;
import java.util.Locale;

public abstract class AbstractLocaleResolver<T> implements LocaleResolver<T> {
   protected final Locale defaultLocale;

   public AbstractLocaleResolver(Locale defaultLocale) {
      this.defaultLocale = defaultLocale;
   }

   @NonNull
   @Override
   public Locale resolveOrDefault(@NonNull T request) {
      return (Locale)this.resolve(request).orElse(this.defaultLocale);
   }
}
