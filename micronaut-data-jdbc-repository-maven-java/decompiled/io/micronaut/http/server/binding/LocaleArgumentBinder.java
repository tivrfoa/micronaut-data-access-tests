package io.micronaut.http.server.binding;

import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import io.micronaut.http.server.util.locale.HttpLocaleResolver;
import jakarta.inject.Singleton;
import java.util.Locale;
import java.util.Optional;

@Singleton
public class LocaleArgumentBinder implements TypedRequestArgumentBinder<Locale> {
   private final HttpLocaleResolver localeResolver;

   public LocaleArgumentBinder(HttpLocaleResolver localeResolver) {
      this.localeResolver = localeResolver;
   }

   @Override
   public Argument<Locale> argumentType() {
      return Argument.of(Locale.class);
   }

   public ArgumentBinder.BindingResult<Locale> bind(ArgumentConversionContext<Locale> context, HttpRequest<?> source) {
      Optional<Locale> locale = this.localeResolver.resolve(source);
      return locale.isPresent() ? () -> locale : ArgumentBinder.BindingResult.UNSATISFIED;
   }
}
