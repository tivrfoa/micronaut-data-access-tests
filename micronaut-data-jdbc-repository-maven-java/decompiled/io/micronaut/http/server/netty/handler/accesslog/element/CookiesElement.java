package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import java.util.List;
import java.util.StringJoiner;

final class CookiesElement extends AbstractHttpMessageLogElement {
   public static final String REQUEST_COOKIES = "C";
   public static final String RESPONSE_COOKIES = "c";
   private static final CookiesElement REQUEST_COOKIES_ELEMENT = new CookiesElement("Cookie");
   private static final CookiesElement RESPONSE_COOKIES_ELEMENT = new CookiesElement("Set-Cookie");
   private final String headerName;

   private CookiesElement(String headerName) {
      if (!"Cookie".equals(headerName) && !"Set-Cookie".equals(headerName)) {
         this.headerName = "Cookie";
      } else {
         this.headerName = headerName;
      }

      this.events = "Cookie".equals(this.headerName) ? LogElement.Event.REQUEST_HEADERS_EVENTS : LogElement.Event.RESPONSE_HEADERS_EVENTS;
   }

   public static CookiesElement forRequest() {
      return REQUEST_COOKIES_ELEMENT;
   }

   public static CookiesElement forResponse() {
      return RESPONSE_COOKIES_ELEMENT;
   }

   @Override
   protected String value(HttpHeaders headers) {
      String header = headers.get(this.headerName);
      if (header == null) {
         return "-";
      } else {
         List<Cookie> cookies = ServerCookieDecoder.STRICT.decodeAll(header);
         if (cookies.isEmpty()) {
            return "-";
         } else if (cookies.size() == 1) {
            Cookie cookie = (Cookie)cookies.iterator().next();
            return cookie.name() + ':' + cookie.value();
         } else {
            StringJoiner joiner = new StringJoiner(",", "[", "]");

            for(Cookie cookie : cookies) {
               joiner.add(cookie.name() + ':' + cookie.value());
            }

            return joiner.toString();
         }
      }
   }

   public String toString() {
      return '%' + ("Cookie".equals(this.headerName) ? "C" : "c");
   }
}
