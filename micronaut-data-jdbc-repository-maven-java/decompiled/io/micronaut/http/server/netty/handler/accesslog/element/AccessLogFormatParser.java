package io.micronaut.http.server.netty.handler.accesslog.element;

import io.micronaut.core.io.service.ServiceDefinition;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.core.order.OrderUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessLogFormatParser {
   public static final String COMBINED_LOG_FORMAT = "%h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\"";
   public static final String COMMON_LOG_FORMAT = "%h %l %u %t \"%r\" %s %b";
   private static final List<LogElementBuilder> LOG_ELEMENT_BUILDERS = new ArrayList();
   private static final Logger LOGGER = LoggerFactory.getLogger(AccessLogFormatParser.class);
   private final List<AccessLogFormatParser.IndexedLogElement> onRequestElements = new ArrayList();
   private final List<AccessLogFormatParser.IndexedLogElement> onResponseHeadersElements = new ArrayList();
   private final List<AccessLogFormatParser.IndexedLogElement> onResponseWriteElements = new ArrayList();
   private final List<AccessLogFormatParser.IndexedLogElement> onLastResponseWriteElements = new ArrayList();
   private final List<AccessLogFormatParser.IndexedLogElement> constantElements = new ArrayList();
   private String[] elements;

   public AccessLogFormatParser(String spec) {
      this.parse(spec);
   }

   public AccessLog newAccessLogger() {
      String[] newElements = new String[this.elements.length];
      System.arraycopy(this.elements, 0, newElements, 0, this.elements.length);
      Map<AccessLogFormatParser.IndexedLogElement, AccessLogFormatParser.IndexedLogElement> map = new IdentityHashMap();
      return new AccessLog(
         copy(map, this.onRequestElements),
         copy(map, this.onResponseHeadersElements),
         copy(map, this.onResponseWriteElements),
         copy(map, this.onLastResponseWriteElements),
         newElements
      );
   }

   public String toString() {
      SortedSet<AccessLogFormatParser.IndexedLogElement> elts = new TreeSet();
      elts.addAll(this.constantElements);
      elts.addAll(this.onLastResponseWriteElements);
      elts.addAll(this.onRequestElements);
      elts.addAll(this.onResponseHeadersElements);
      elts.addAll(this.onResponseWriteElements);
      return (String)elts.stream().map(AccessLogFormatParser.IndexedLogElement::toString).collect(Collectors.joining());
   }

   private static List<AccessLogFormatParser.IndexedLogElement> copy(
      Map<AccessLogFormatParser.IndexedLogElement, AccessLogFormatParser.IndexedLogElement> map, List<AccessLogFormatParser.IndexedLogElement> l
   ) {
      return (List<AccessLogFormatParser.IndexedLogElement>)l.stream()
         .map(elt -> (AccessLogFormatParser.IndexedLogElement)map.computeIfAbsent(elt, AccessLogFormatParser.IndexedLogElement::copyIndexedLogElement))
         .collect(Collectors.toList());
   }

   private void parse(String spec) {
      if (spec == null || spec.isEmpty() || "common".equals(spec)) {
         spec = "%h %l %u %t \"%r\" %s %b";
      } else if ("combined".equals(spec)) {
         spec = "%h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\"";
      }

      List<LogElement> logElements = this.tokenize(spec);
      this.elements = new String[logElements.size()];

      for(int i = 0; i < this.elements.length; ++i) {
         LogElement element = (LogElement)logElements.get(i);
         AccessLogFormatParser.IndexedLogElement indexedLogElement = new AccessLogFormatParser.IndexedLogElement(element, i);
         if (element.events().isEmpty()) {
            this.constantElements.add(indexedLogElement);
            this.elements[i] = element.onRequestHeaders(null, null, null, null, null);
         } else {
            if (element.events().contains(LogElement.Event.ON_LAST_RESPONSE_WRITE)) {
               this.onLastResponseWriteElements.add(indexedLogElement);
            }

            if (element.events().contains(LogElement.Event.ON_REQUEST_HEADERS)) {
               this.onRequestElements.add(indexedLogElement);
            }

            if (element.events().contains(LogElement.Event.ON_RESPONSE_HEADERS)) {
               this.onResponseHeadersElements.add(indexedLogElement);
            }

            if (element.events().contains(LogElement.Event.ON_RESPONSE_WRITE)) {
               this.onResponseWriteElements.add(indexedLogElement);
            }
         }
      }

      trimToSize(this.onLastResponseWriteElements);
      trimToSize(this.onRequestElements);
      trimToSize(this.onResponseHeadersElements);
      trimToSize(this.onResponseWriteElements);
      trimToSize(this.constantElements);
   }

   private static <T> void trimToSize(List<T> l) {
      ((ArrayList)l).trimToSize();
   }

   private List<LogElement> tokenize(String spec) {
      List<LogElement> logElements = new ArrayList();
      spec = spec.trim();
      int state = 0;
      StringBuilder token = new StringBuilder(40);

      for(int i = 0; i < spec.length(); ++i) {
         char c = spec.charAt(i);
         state = this.nextState(logElements, state, token, c);
      }

      if (state == 0 && !logElements.isEmpty()) {
         this.checkConstantElement(logElements, token);
         return logElements;
      } else {
         LOGGER.warn("Invalid access log format: {}", spec);
         throw new IllegalArgumentException("Invalid access log format: " + spec);
      }
   }

   private int nextState(List<LogElement> logElements, int state, StringBuilder token, char c) {
      switch(state) {
         case 0:
            if (c == '%') {
               state = 1;
            } else {
               token.append(c);
            }
            break;
         case 1:
            if (c == '{') {
               this.checkConstantElement(logElements, token);
               state = 2;
            } else if (c == '%') {
               token.append(c);
               state = 0;
            } else {
               this.checkConstantElement(logElements, token);
               logElements.add(this.fromToken(Character.toString(c), null));
               state = 0;
            }
            break;
         case 2:
            if (c == '}') {
               state = 3;
            } else {
               token.append(c);
            }
            break;
         case 3:
            String param = token.toString();
            logElements.add(this.fromToken(Character.toString(c), param));
            token.setLength(0);
            state = 0;
      }

      return state;
   }

   private void checkConstantElement(List<LogElement> logElements, StringBuilder token) {
      if (token.length() != 0) {
         logElements.add(new ConstantElement(token.toString()));
         token.setLength(0);
      }

   }

   private LogElement fromToken(String pattern, String param) {
      for(LogElementBuilder builder : LOG_ELEMENT_BUILDERS) {
         LogElement logElement = builder.build(pattern, param);
         if (logElement != null) {
            return logElement;
         }
      }

      LOGGER.warn("Unknown access log marker: %{}", pattern);
      return ConstantElement.UNKNOWN;
   }

   static {
      SoftServiceLoader<LogElementBuilder> builders = SoftServiceLoader.load(LogElementBuilder.class, LogElementBuilder.class.getClassLoader());

      for(ServiceDefinition<LogElementBuilder> definition : builders) {
         if (definition.isPresent()) {
            LOG_ELEMENT_BUILDERS.add(definition.load());
         }
      }

      OrderUtil.sort(LOG_ELEMENT_BUILDERS);
      trimToSize(LOG_ELEMENT_BUILDERS);
   }

   static class IndexedLogElement implements LogElement, Comparable<AccessLogFormatParser.IndexedLogElement> {
      final int index;
      private final LogElement delegate;

      IndexedLogElement(LogElement delegate, int index) {
         this.delegate = delegate;
         this.index = index;
      }

      @Override
      public Set<LogElement.Event> events() {
         return this.delegate.events();
      }

      @Override
      public void reset() {
         this.delegate.reset();
      }

      @Override
      public String onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
         return this.delegate.onRequestHeaders(channel, method, headers, uri, protocol);
      }

      @Override
      public String onResponseHeaders(ChannelHandlerContext ctx, HttpHeaders headers, String status) {
         return this.delegate.onResponseHeaders(ctx, headers, status);
      }

      @Override
      public void onResponseWrite(int contentSize) {
         this.delegate.onResponseWrite(contentSize);
      }

      @Override
      public String onLastResponseWrite(int contentSize) {
         return this.delegate.onLastResponseWrite(contentSize);
      }

      @Override
      public LogElement copy() {
         return new AccessLogFormatParser.IndexedLogElement(this.delegate.copy(), this.index);
      }

      public AccessLogFormatParser.IndexedLogElement copyIndexedLogElement() {
         return new AccessLogFormatParser.IndexedLogElement(this.delegate.copy(), this.index);
      }

      public int compareTo(AccessLogFormatParser.IndexedLogElement o) {
         return Long.compare((long)this.index, (long)o.index);
      }

      public String toString() {
         return this.delegate.toString();
      }

      public int hashCode() {
         return this.index;
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (this.getClass() != obj.getClass()) {
            return false;
         } else {
            AccessLogFormatParser.IndexedLogElement other = (AccessLogFormatParser.IndexedLogElement)obj;
            return this.index == other.index;
         }
      }
   }
}
