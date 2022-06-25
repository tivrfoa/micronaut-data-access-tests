package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.handler.codec.http.HttpHeaders;
import java.util.List;
import java.util.StringJoiner;

final class HeaderElement extends AbstractHttpMessageLogElement {
   public static final String REQUEST_HEADER = "i";
   public static final String RESPONSE_HEADER = "o";
   private final String header;

   HeaderElement(boolean onRequest, final String header) {
      this.header = header;
      this.events = onRequest ? LogElement.Event.REQUEST_HEADERS_EVENTS : LogElement.Event.RESPONSE_HEADERS_EVENTS;
   }

   @Override
   protected String value(HttpHeaders headers) {
      List<String> values = headers.getAllAsString(this.header);
      if (values.isEmpty()) {
         return "-";
      } else if (values.size() == 1) {
         return (String)values.iterator().next();
      } else {
         StringJoiner joiner = new StringJoiner(",", "[", "]");

         for(String v : values) {
            joiner.add(v);
         }

         return joiner.toString();
      }
   }

   public String toString() {
      return "%{" + this.header + '}' + (this.events.contains(LogElement.Event.ON_REQUEST_HEADERS) ? "i" : "o");
   }
}
