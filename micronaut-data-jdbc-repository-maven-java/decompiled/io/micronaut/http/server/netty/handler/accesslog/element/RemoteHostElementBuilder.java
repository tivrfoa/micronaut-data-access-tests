package io.micronaut.http.server.netty.handler.accesslog.element;

public final class RemoteHostElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      return "h".equals(token) ? RemoteHostElement.INSTANCE : null;
   }
}
