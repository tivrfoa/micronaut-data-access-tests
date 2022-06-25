package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Set;

final class LocalIpElement implements LogElement {
   public static final String LOCAL_IP = "A";
   static final LocalIpElement INSTANCE = new LocalIpElement();

   private LocalIpElement() {
   }

   @Override
   public Set<LogElement.Event> events() {
      return LogElement.Event.REQUEST_HEADERS_EVENTS;
   }

   @Override
   public String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      return channel.localAddress().getAddress().getHostAddress();
   }

   @Override
   public LogElement copy() {
      return this;
   }

   public String toString() {
      return "%A";
   }
}
