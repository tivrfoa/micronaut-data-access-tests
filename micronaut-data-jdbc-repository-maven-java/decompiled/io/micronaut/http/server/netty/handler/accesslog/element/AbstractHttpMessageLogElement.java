package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Set;

abstract class AbstractHttpMessageLogElement implements LogElement {
   protected Set<LogElement.Event> events;

   protected abstract String value(HttpHeaders headers);

   private static String wrapValue(String value) {
      if (value != null && !"-".equals(value) && !value.isEmpty()) {
         StringBuilder buffer = new StringBuilder(value.length() + 2);
         buffer.append('\'');
         int i = 0;

         while(i < value.length()) {
            int j = value.indexOf(39, i);
            if (j == -1) {
               buffer.append(value.substring(i));
               i = value.length();
            } else {
               buffer.append(value.substring(i, j + 1));
               buffer.append('"');
               i = j + 2;
            }
         }

         buffer.append('\'');
         return buffer.toString();
      } else {
         return "-";
      }
   }

   @Override
   public Set<LogElement.Event> events() {
      return this.events;
   }

   @Override
   public String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      return this.events.contains(LogElement.Event.ON_REQUEST_HEADERS) ? wrapValue(this.value(headers)) : "-";
   }

   @Override
   public String onResponseHeaders(ChannelHandlerContext ctx, HttpHeaders headers, String status) {
      return this.events.contains(LogElement.Event.ON_RESPONSE_HEADERS) ? wrapValue(this.value(headers)) : "-";
   }

   @Override
   public LogElement copy() {
      return this;
   }
}
