package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Set;

final class LocalHostElement implements LogElement {
   public static final String LOCAL_HOST = "v";
   static final LocalHostElement INSTANCE = new LocalHostElement();

   private LocalHostElement() {
   }

   @Override
   public Set<LogElement.Event> events() {
      return LogElement.Event.REQUEST_HEADERS_EVENTS;
   }

   @Override
   public String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      return channel.localAddress().getAddress().getHostName();
   }

   @Override
   public LogElement copy() {
      return this;
   }

   public String toString() {
      return "%v";
   }
}
