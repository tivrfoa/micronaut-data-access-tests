package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class ConstantElement implements LogElement {
   public static final String UNKNOWN_VALUE = "-";
   public static final ConstantElement UNKNOWN = new ConstantElement("-");
   private static final Set<LogElement.Event> EVENTS = Collections.unmodifiableSet(EnumSet.noneOf(LogElement.Event.class));
   private final String value;

   ConstantElement(final String value) {
      this.value = value;
   }

   @Override
   public String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      return this.value;
   }

   @Override
   public Set<LogElement.Event> events() {
      return EVENTS;
   }

   @Override
   public LogElement copy() {
      return this;
   }

   public String toString() {
      return this.value.replace("%", "%%");
   }
}
