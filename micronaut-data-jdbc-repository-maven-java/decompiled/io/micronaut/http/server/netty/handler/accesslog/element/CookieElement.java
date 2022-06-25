package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

final class CookieElement extends AbstractHttpMessageLogElement {
   public static final String REQUEST_COOKIE = "C";
   public static final String RESPONSE_COOKIE = "c";
   private final String headerName;
   private final String cookieName;

   CookieElement(boolean forRequest, final String cookieName) {
      this.cookieName = cookieName;
      this.headerName = forRequest ? HttpHeaderNames.COOKIE.toString() : HttpHeaderNames.SET_COOKIE.toString();
      this.events = forRequest ? LogElement.Event.REQUEST_HEADERS_EVENTS : LogElement.Event.RESPONSE_HEADERS_EVENTS;
   }

   @Override
   protected String value(HttpHeaders headers) {
      String header = headers.get(this.headerName);
      if (header != null) {
         for(Cookie cookie : ServerCookieDecoder.STRICT.decodeAll(header)) {
            if (this.cookieName.equals(cookie.name())) {
               return cookie.value();
            }
         }
      }

      return "-";
   }

   @Override
   public LogElement copy() {
      return this;
   }

   public String toString() {
      return "%{" + this.cookieName + '}' + (HttpHeaderNames.COOKIE.toString().equals(this.headerName) ? "C" : "c");
   }
}
