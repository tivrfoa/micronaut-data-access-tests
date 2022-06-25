package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Set;

final class LocalPortElement implements LogElement {
   public static final String LOCAL_PORT = "p";
   static final LocalPortElement INSTANCE = new LocalPortElement();

   private LocalPortElement() {
   }

   @Override
   public Set<LogElement.Event> events() {
      return LogElement.Event.REQUEST_HEADERS_EVENTS;
   }

   @Override
   public String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      return Integer.toString(channel.localAddress().getPort());
   }

   @Override
   public LogElement copy() {
      return this;
   }

   public String toString() {
      return "%p";
   }
}
