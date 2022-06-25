package io.micronaut.http.simple.cookies;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SimpleCookies implements Cookies {
   private final ConversionService<?> conversionService;
   private final Map<CharSequence, Cookie> cookies;

   public SimpleCookies(ConversionService conversionService) {
      this.conversionService = conversionService;
      this.cookies = new LinkedHashMap();
   }

   @Override
   public Set<Cookie> getAll() {
      return new HashSet(this.cookies.values());
   }

   @Override
   public Optional<Cookie> findCookie(CharSequence name) {
      Cookie cookie = (Cookie)this.cookies.get(name);
      return cookie != null ? Optional.of(cookie) : Optional.empty();
   }

   @Override
   public <T> Optional<T> get(CharSequence name, Class<T> requiredType) {
      return requiredType != Cookie.class && requiredType != Object.class
         ? this.findCookie(name).flatMap(cookie -> this.conversionService.convert(cookie.getValue(), requiredType))
         : this.findCookie(name);
   }

   @Override
   public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
      return this.findCookie(name).flatMap(cookie -> this.conversionService.convert(cookie.getValue(), conversionContext));
   }

   @Override
   public Collection<Cookie> values() {
      return Collections.unmodifiableCollection(this.cookies.values());
   }

   public Cookie put(CharSequence name, Cookie cookie) {
      return (Cookie)this.cookies.put(name, cookie);
   }

   public void putAll(Map<CharSequence, Cookie> cookies) {
      this.cookies.putAll(cookies);
   }
}
