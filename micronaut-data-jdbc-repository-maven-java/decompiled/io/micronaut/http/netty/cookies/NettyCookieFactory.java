package io.micronaut.http.netty.cookies;

import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.CookieFactory;

public class NettyCookieFactory implements CookieFactory {
   @Override
   public Cookie create(String name, String value) {
      return new NettyCookie(name, value);
   }
}
