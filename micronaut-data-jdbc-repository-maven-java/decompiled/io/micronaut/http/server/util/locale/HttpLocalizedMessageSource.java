package io.micronaut.http.server.util.locale;

import io.micronaut.context.AbstractLocalizedMessageSource;
import io.micronaut.context.MessageSource;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.LocaleResolver;
import io.micronaut.http.HttpRequest;
import io.micronaut.runtime.http.scope.RequestAware;
import io.micronaut.runtime.http.scope.RequestScope;
import java.util.Locale;

@RequestScope
public class HttpLocalizedMessageSource extends AbstractLocalizedMessageSource<HttpRequest<?>> implements RequestAware {
   private Locale locale;

   public HttpLocalizedMessageSource(LocaleResolver<HttpRequest<?>> localeResolver, MessageSource messageSource) {
      super(localeResolver, messageSource);
   }

   @NonNull
   @Override
   protected Locale getLocale() {
      if (this.locale == null) {
         throw new IllegalStateException("RequestAware::setRequest should have set the locale");
      } else {
         return this.locale;
      }
   }

   @Override
   public void setRequest(HttpRequest<?> request) {
      this.locale = this.resolveLocale(request);
   }
}
