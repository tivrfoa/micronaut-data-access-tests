package io.micronaut.http.server.netty.handler.accesslog.element;

public final class RequestUriElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      return "x".equals(token) ? RequestUriElement.INSTANCE : null;
   }
}
