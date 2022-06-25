package io.micronaut.http.server.netty.handler.accesslog.element;

public final class ElapseTimeElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      if ("D".equals(token)) {
         return new ElapseTimeElement(false);
      } else {
         return "T".equals(token) ? new ElapseTimeElement(true) : null;
      }
   }
}
