package io.micronaut.http.server.netty.handler.accesslog.element;

public final class RequestLineElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      return "r".equals(token) ? RequestLineElement.INSTANCE : null;
   }
}
