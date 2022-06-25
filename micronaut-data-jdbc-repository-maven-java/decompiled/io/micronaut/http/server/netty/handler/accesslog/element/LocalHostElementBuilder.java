package io.micronaut.http.server.netty.handler.accesslog.element;

public final class LocalHostElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      return "v".equals(token) ? LocalHostElement.INSTANCE : null;
   }
}
