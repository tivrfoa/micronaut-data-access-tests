package io.micronaut.http.server.netty.handler.accesslog.element;

public final class HeaderElementBuilder implements LogElementBuilder {
   @Override
   public LogElement build(String token, String param) {
      if ("i".equals(token)) {
         return (LogElement)(param == null ? HeadersElement.forRequest() : new HeaderElement(true, param));
      } else if ("o".equals(token)) {
         return (LogElement)(param == null ? HeadersElement.forResponse() : new HeaderElement(false, param));
      } else {
         return null;
      }
   }
}
