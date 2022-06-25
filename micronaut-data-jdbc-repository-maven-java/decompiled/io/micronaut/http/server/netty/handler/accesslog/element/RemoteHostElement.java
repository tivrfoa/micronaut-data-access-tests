package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Set;

final class RemoteHostElement implements LogElement {
   public static final String REMOTE_HOST = "h";
   static final RemoteHostElement INSTANCE = new RemoteHostElement();

   private RemoteHostElement() {
   }

   @Override
   public Set<LogElement.Event> events() {
      return LogElement.Event.REQUEST_HEADERS_EVENTS;
   }

   @Override
   public String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      return channel.remoteAddress().getAddress().getHostName();
   }

   @Override
   public LogElement copy() {
      return this;
   }

   public String toString() {
      return "%h";
   }
}
