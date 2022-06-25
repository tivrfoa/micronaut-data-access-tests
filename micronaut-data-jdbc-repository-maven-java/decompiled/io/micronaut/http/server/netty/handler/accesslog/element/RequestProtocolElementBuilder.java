package io.micronaut.http.server.netty.handler.accesslog.element;

public final class RequestProtocolElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      return "H".equals(token) ? RequestProtocolElement.INSTANCE : null;
   }
}
