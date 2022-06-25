package io.micronaut.http.server.netty.handler.accesslog.element;

public final class CookieElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      if ("C".equals(token)) {
         return (LogElement)(param == null ? CookiesElement.forRequest() : new CookieElement(true, param));
      } else if ("c".equals(token)) {
         return (LogElement)(param == null ? CookiesElement.forResponse() : new CookieElement(false, param));
      } else {
         return null;
      }
   }
}
