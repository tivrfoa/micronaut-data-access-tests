package io.micronaut.http.server.util.locale;

import io.micronaut.core.util.locale.AbstractLocaleResolver;
import io.micronaut.http.HttpRequest;

public abstract class HttpAbstractLocaleResolver extends AbstractLocaleResolver<HttpRequest<?>> implements HttpLocaleResolver {
   public static final Integer ORDER = 50;
   protected HttpLocaleResolutionConfiguration httpLocaleResolutionConfiguration;

   public HttpAbstractLocaleResolver(HttpLocaleResolutionConfiguration httpLocaleResolutionConfiguration) {
      super(httpLocaleResolutionConfiguration.getDefaultLocale());
      this.httpLocaleResolutionConfiguration = httpLocaleResolutionConfiguration;
   }

   @Override
   public int getOrder() {
      return ORDER;
   }
}
