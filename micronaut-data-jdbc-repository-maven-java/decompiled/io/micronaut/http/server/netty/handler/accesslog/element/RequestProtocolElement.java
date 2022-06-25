package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Set;

final class RequestProtocolElement implements LogElement {
   public static final String REQUEST_PROTOCOL = "H";
   static final RequestProtocolElement INSTANCE = new RequestProtocolElement();

   private RequestProtocolElement() {
   }

   @Override
   public LogElement copy() {
      return this;
   }

   @Override
   public String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      return protocol;
   }

   @Override
   public Set<LogElement.Event> events() {
      return LogElement.Event.REQUEST_HEADERS_EVENTS;
   }

   public String toString() {
      return "HH";
   }
}
