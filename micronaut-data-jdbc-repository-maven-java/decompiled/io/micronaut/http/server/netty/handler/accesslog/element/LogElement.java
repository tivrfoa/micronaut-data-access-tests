package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public interface LogElement {
   Set<LogElement.Event> events();

   default void reset() {
   }

   default String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      return "-";
   }

   default String onResponseHeaders(ChannelHandlerContext ctx, HttpHeaders headers, String status) {
      return "-";
   }

   default void onResponseWrite(int bytesSent) {
   }

   default String onLastResponseWrite(int bytesSent) {
      return "-";
   }

   LogElement copy();

   public static enum Event {
      ON_REQUEST_HEADERS,
      ON_RESPONSE_HEADERS,
      ON_RESPONSE_WRITE,
      ON_LAST_RESPONSE_WRITE;

      public static final Set<LogElement.Event> REQUEST_HEADERS_EVENTS = Collections.unmodifiableSet(EnumSet.of(ON_REQUEST_HEADERS));
      public static final Set<LogElement.Event> RESPONSE_HEADERS_EVENTS = Collections.unmodifiableSet(EnumSet.of(ON_RESPONSE_HEADERS));
   }
}
