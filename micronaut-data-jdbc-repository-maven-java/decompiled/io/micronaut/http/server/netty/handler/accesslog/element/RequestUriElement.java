package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Set;

final class RequestUriElement implements LogElement {
   public static final String REQUEST_URI = "x";
   static final RequestUriElement INSTANCE = new RequestUriElement();

   private RequestUriElement() {
   }

   @Override
   public LogElement copy() {
      return this;
   }

   @Override
   public String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      return uri;
   }

   @Override
   public Set<LogElement.Event> events() {
      return LogElement.Event.REQUEST_HEADERS_EVENTS;
   }

   public String toString() {
      return "%x";
   }
}
