package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Set;

final class ResponseCodeElement implements LogElement {
   public static final String RESPONSE_CODE = "s";
   static final ResponseCodeElement INSTANCE = new ResponseCodeElement();

   private ResponseCodeElement() {
   }

   @Override
   public LogElement copy() {
      return this;
   }

   @Override
   public String onResponseHeaders(ChannelHandlerContext ctx, HttpHeaders headers, String status) {
      return status;
   }

   @Override
   public Set<LogElement.Event> events() {
      return LogElement.Event.RESPONSE_HEADERS_EVENTS;
   }

   public String toString() {
      return "%s";
   }
}
