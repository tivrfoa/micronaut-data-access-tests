package io.micronaut.http.server.netty.handler.accesslog.element;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

final class BytesSentElement implements LogElement {
   public static final String BYTES_SENT_DASH = "b";
   public static final String BYTES_SENT = "B";
   private static final Set<LogElement.Event> EVENTS = Collections.unmodifiableSet(
      EnumSet.of(LogElement.Event.ON_RESPONSE_WRITE, LogElement.Event.ON_LAST_RESPONSE_WRITE)
   );
   private final boolean dashIfZero;
   private long bytesSent;

   BytesSentElement(final boolean dashIfZero) {
      this.dashIfZero = dashIfZero;
   }

   @Override
   public void onResponseWrite(int contentSize) {
      this.bytesSent += (long)contentSize;
   }

   @Override
   public String onLastResponseWrite(int contentSize) {
      this.bytesSent += (long)contentSize;
      return this.dashIfZero && this.bytesSent == 0L ? "-" : Long.toString(this.bytesSent);
   }

   @Override
   public Set<LogElement.Event> events() {
      return EVENTS;
   }

   @Override
   public LogElement copy() {
      return new BytesSentElement(this.dashIfZero);
   }

   @Override
   public void reset() {
      this.bytesSent = 0L;
   }

   public String toString() {
      return '%' + (this.dashIfZero ? "b" : "B");
   }
}
