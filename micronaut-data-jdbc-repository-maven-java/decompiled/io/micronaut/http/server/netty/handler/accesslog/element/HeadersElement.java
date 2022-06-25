package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.handler.codec.http.HttpHeaders;
import java.util.StringJoiner;
import java.util.Map.Entry;

final class HeadersElement extends AbstractHttpMessageLogElement {
   private static final HeadersElement REQUEST_HEADERS_ELEMENT = new HeadersElement(true);
   private static final HeadersElement RESPONSE_HEADERS_ELEMENT = new HeadersElement(false);

   private HeadersElement(boolean onRequest) {
      this.events = onRequest ? LogElement.Event.REQUEST_HEADERS_EVENTS : LogElement.Event.RESPONSE_HEADERS_EVENTS;
   }

   public static HeadersElement forRequest() {
      return REQUEST_HEADERS_ELEMENT;
   }

   public static HeadersElement forResponse() {
      return RESPONSE_HEADERS_ELEMENT;
   }

   @Override
   protected String value(HttpHeaders headers) {
      if (headers.isEmpty()) {
         return "-";
      } else if (headers.size() == 1) {
         Entry<CharSequence, CharSequence> header = (Entry)headers.iteratorCharSequence().next();
         return header.getKey() + ":" + header.getValue();
      } else {
         StringJoiner joiner = new StringJoiner(",", "[", "]");
         headers.forEach(header -> joiner.add((String)header.getKey() + ':' + (String)header.getValue()));
         return joiner.toString();
      }
   }

   public String toString() {
      return this.events.contains(LogElement.Event.ON_REQUEST_HEADERS) ? "i" : "o";
   }
}
