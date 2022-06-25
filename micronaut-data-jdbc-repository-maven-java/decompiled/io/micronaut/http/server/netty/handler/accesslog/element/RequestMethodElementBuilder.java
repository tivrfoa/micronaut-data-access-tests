package io.micronaut.http.server.netty.handler.accesslog.element;

public final class RequestMethodElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      return "m".equals(token) ? RequestMethodElement.INSTANCE : null;
   }
}
