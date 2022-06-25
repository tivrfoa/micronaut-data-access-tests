package io.micronaut.http.simple.cookies;

import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.CookieFactory;

public class SimpleCookieFactory implements CookieFactory {
   @Override
   public Cookie create(String name, String value) {
      return new SimpleCookie(name, value);
   }
}
