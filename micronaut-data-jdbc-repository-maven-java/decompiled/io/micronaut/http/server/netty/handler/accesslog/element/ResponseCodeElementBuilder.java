package io.micronaut.http.server.netty.handler.accesslog.element;

public final class ResponseCodeElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      return "s".equals(token) ? ResponseCodeElement.INSTANCE : null;
   }
}
