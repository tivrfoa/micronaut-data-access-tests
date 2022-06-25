package io.micronaut.context;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.LocaleResolver;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractLocalizedMessageSource<T> implements LocalizedMessageSource {
   private final LocaleResolver<T> localeResolver;
   private final MessageSource messageSource;

   public AbstractLocalizedMessageSource(LocaleResolver<T> localeResolver, MessageSource messageSource) {
      this.localeResolver = localeResolver;
      this.messageSource = messageSource;
   }

   @NonNull
   protected abstract Locale getLocale();

   @NonNull
   @Override
   public Optional<String> getMessage(@NonNull String code, Object... variables) {
      return this.messageSource.getMessage(code, this.getLocale(), variables);
   }

   @NonNull
   @Override
   public Optional<String> getMessage(@NonNull String code, Map<String, Object> variables) {
      return this.messageSource.getMessage(code, this.getLocale(), variables);
   }

   @NonNull
   @Override
   public Optional<String> getMessage(@NonNull String code) {
      return this.messageSource.getMessage(code, this.getLocale());
   }

   @NonNull
   protected Locale resolveLocale(T localeResolutionContext) {
      return this.localeResolver.resolveOrDefault(localeResolutionContext);
   }
}
