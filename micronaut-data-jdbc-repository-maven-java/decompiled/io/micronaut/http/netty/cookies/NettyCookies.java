package io.micronaut.http.netty.cookies;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyCookies implements Cookies {
   private static final Logger LOG = LoggerFactory.getLogger(NettyCookies.class);
   private final ConversionService<?> conversionService;
   private final Map<CharSequence, Cookie> cookies;

   public NettyCookies(String path, HttpHeaders nettyHeaders, ConversionService conversionService) {
      this.conversionService = conversionService;
      String value = nettyHeaders.get(HttpHeaderNames.COOKIE);
      if (value != null) {
         this.cookies = new LinkedHashMap();

         for(io.netty.handler.codec.http.cookie.Cookie nettyCookie : ServerCookieDecoder.LAX.decode(value)) {
            String cookiePath = nettyCookie.path();
            if (cookiePath != null) {
               if (path.startsWith(cookiePath)) {
                  this.cookies.put(nettyCookie.name(), new NettyCookie(nettyCookie));
               }
            } else {
               this.cookies.put(nettyCookie.name(), new NettyCookie(nettyCookie));
            }
         }
      } else {
         this.cookies = Collections.emptyMap();
      }

   }

   public NettyCookies(HttpHeaders nettyHeaders, ConversionService conversionService) {
      this.conversionService = conversionService;
      if (nettyHeaders != null) {
         List<String> values = nettyHeaders.getAll(HttpHeaderNames.SET_COOKIE);
         if (values != null && !values.isEmpty()) {
            this.cookies = new LinkedHashMap();

            for(String value : values) {
               io.netty.handler.codec.http.cookie.Cookie nettyCookie = ClientCookieDecoder.STRICT.decode(value);
               if (nettyCookie != null) {
                  this.cookies.put(nettyCookie.name(), new NettyCookie(nettyCookie));
               } else if (LOG.isTraceEnabled()) {
                  LOG.trace("Failed to decode cookie value [{}]", value);
               }
            }
         } else {
            this.cookies = Collections.emptyMap();
         }
      } else {
         this.cookies = Collections.emptyMap();
      }

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
}
