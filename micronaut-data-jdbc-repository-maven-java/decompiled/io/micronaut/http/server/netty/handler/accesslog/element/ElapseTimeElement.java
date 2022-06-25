package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

final class ElapseTimeElement implements LogElement {
   public static final String ELAPSE_TIME_SECONDS = "T";
   public static final String ELAPSE_TIME_MILLIS = "D";
   private static final Set<LogElement.Event> EVENTS = Collections.unmodifiableSet(
      EnumSet.of(LogElement.Event.ON_REQUEST_HEADERS, LogElement.Event.ON_LAST_RESPONSE_WRITE)
   );
   private final boolean inSeconds;
   private long start;

   ElapseTimeElement(final boolean inSeconds) {
      this.inSeconds = inSeconds;
   }

   @Override
   public String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      this.start = System.nanoTime();
      return null;
   }

   @Override
   public String onLastResponseWrite(int contentSize) {
      long elapseTime = this.inSeconds
         ? TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - this.start)
         : TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - this.start);
      return Long.toString(elapseTime);
   }

   @Override
   public Set<LogElement.Event> events() {
      return EVENTS;
   }

   @Override
   public LogElement copy() {
      return new ElapseTimeElement(this.inSeconds);
   }

   @Override
   public void reset() {
      this.start = 0L;
   }

   public String toString() {
      return '%' + (this.inSeconds ? "T" : "D");
   }
}
