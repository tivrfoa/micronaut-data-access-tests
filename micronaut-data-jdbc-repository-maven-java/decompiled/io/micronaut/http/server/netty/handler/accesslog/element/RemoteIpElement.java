package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Locale;
import java.util.Set;

final class RemoteIpElement implements LogElement {
   public static final String REMOTE_IP = "a";
   public static final String X_FORWARDED_FOR = "X-Forwarded-For";
   static final RemoteIpElement INSTANCE = new RemoteIpElement();

   private RemoteIpElement() {
   }

   @Override
   public LogElement copy() {
      return this;
   }

   @Override
   public Set<LogElement.Event> events() {
      return LogElement.Event.REQUEST_HEADERS_EVENTS;
   }

   @Override
   public String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      String xforwardedFor = headers.get("X-Forwarded-For", null);
      if (xforwardedFor == null) {
         String forwarded = headers.get("Forwarded", null);
         if (forwarded != null) {
            String inet = processForwarded(forwarded);
            if (inet != null) {
               return inet;
            }
         }

         return channel.remoteAddress().getAddress().getHostAddress();
      } else {
         return processXForwardedFor(xforwardedFor);
      }
   }

   private static String processXForwardedFor(String xforwardedFor) {
      int firstComma = xforwardedFor.indexOf(44);
      return firstComma >= 0 ? xforwardedFor.substring(0, firstComma) : xforwardedFor;
   }

   private static String processForwarded(String forwarded) {
      int firstComma = forwarded.indexOf(44);
      String firstForward = (firstComma >= 0 ? forwarded.substring(0, firstComma) : forwarded).toLowerCase(Locale.US);
      int startIndex = firstForward.indexOf("for");
      if (startIndex == -1) {
         return null;
      } else {
         int semiColonIndex = firstForward.indexOf(59);
         int endIndex = semiColonIndex >= 0 ? semiColonIndex : firstForward.length();

         for(int var7 = startIndex + 4; var7 < endIndex; ++var7) {
            char c = firstForward.charAt(var7);
            if (!Character.isWhitespace(c) && c != '=') {
               return firstForward.substring(var7, endIndex);
            }
         }

         return null;
      }
   }

   public String toString() {
      return "%a";
   }
}
