package io.micronaut.http.server.netty.handler.accesslog.element;

public final class LocalIpElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      return "A".equals(token) ? LocalIpElement.INSTANCE : null;
   }
}
