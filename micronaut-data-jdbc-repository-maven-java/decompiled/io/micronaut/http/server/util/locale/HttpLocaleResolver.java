package io.micronaut.http.server.util.locale;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.util.LocaleResolver;
import io.micronaut.http.HttpRequest;

@Indexed(HttpLocaleResolver.class)
public interface HttpLocaleResolver extends LocaleResolver<HttpRequest<?>> {
}
