package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

final class DateTimeElement implements LogElement {
   public static final String DATE_TIME = "t";
   private static final String COMMON_LOG_PATTERN = "'['dd/MMM/yyyy:HH:mm:ss Z']'";
   private static final Set<LogElement.Event> LAST_RESPONSE_EVENTS = Collections.unmodifiableSet(EnumSet.of(LogElement.Event.ON_LAST_RESPONSE_WRITE));
   private final DateTimeFormatter formatter;
   private final Set<LogElement.Event> events;
   private final String dateFormat;

   DateTimeElement(final String dateFormat) {
      boolean fromStart;
      String format;
      if (dateFormat == null) {
         format = "'['dd/MMM/yyyy:HH:mm:ss Z']'";
         fromStart = true;
      } else {
         fromStart = !dateFormat.startsWith("end:");
         if (dateFormat.startsWith("begin:")) {
            format = dateFormat.substring("begin:".length());
            fromStart = true;
         } else if (dateFormat.startsWith("end:")) {
            format = dateFormat.substring("end:".length());
            fromStart = false;
         } else {
            format = dateFormat;
         }
      }

      this.dateFormat = dateFormat;
      this.formatter = DateTimeFormatter.ofPattern(format, Locale.US);
      this.events = fromStart ? LogElement.Event.REQUEST_HEADERS_EVENTS : LAST_RESPONSE_EVENTS;
   }

   @Override
   public Set<LogElement.Event> events() {
      return this.events;
   }

   @Override
   public String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      return this.events.contains(LogElement.Event.ON_REQUEST_HEADERS) ? ZonedDateTime.now().format(this.formatter) : "-";
   }

   @Override
   public String onLastResponseWrite(int contentSize) {
      return this.events.contains(LogElement.Event.ON_LAST_RESPONSE_WRITE) ? ZonedDateTime.now().format(this.formatter) : "-";
   }

   @Override
   public LogElement copy() {
      return this;
   }

   public String toString() {
      return this.dateFormat == null ? "%t" : "%{" + this.dateFormat + '}' + "t";
   }
}
