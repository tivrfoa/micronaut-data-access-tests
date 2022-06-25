package io.micronaut.http.server.netty.handler.accesslog.element;

public final class BytesSentElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      if ("b".equals(token)) {
         return new BytesSentElement(true);
      } else {
         return "B".equals(token) ? new BytesSentElement(false) : null;
      }
   }
}
