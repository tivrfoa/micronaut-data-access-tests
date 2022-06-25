package io.micronaut.http.server.netty.handler.accesslog.element;

public final class DateTimeElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      return "t".equals(token) ? new DateTimeElement(param) : null;
   }
}
