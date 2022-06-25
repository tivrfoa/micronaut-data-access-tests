package io.micronaut.http.server.netty.handler.accesslog.element;

public final class LocalPortElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      return "p".equals(token) ? LocalPortElement.INSTANCE : null;
   }
}
