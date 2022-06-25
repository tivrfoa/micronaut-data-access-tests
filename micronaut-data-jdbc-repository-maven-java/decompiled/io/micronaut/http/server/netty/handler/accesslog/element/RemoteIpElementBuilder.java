package io.micronaut.http.server.netty.handler.accesslog.element;

public final class RemoteIpElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      return "a".equals(token) ? RemoteIpElement.INSTANCE : null;
   }
}
