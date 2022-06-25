package io.micronaut.http.server.netty.handler.accesslog.element;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.List;
import org.slf4j.Logger;

public class AccessLog {
   private final List<AccessLogFormatParser.IndexedLogElement> onRequestHeadersElements;
   private final List<AccessLogFormatParser.IndexedLogElement> onResponseHeadersElements;
   private final List<AccessLogFormatParser.IndexedLogElement> onResponseWriteElements;
   private final List<AccessLogFormatParser.IndexedLogElement> onLastResponseWriteElements;
   private final String[] elements;

   AccessLog(
      List<AccessLogFormatParser.IndexedLogElement> onRequestHeadersElements,
      List<AccessLogFormatParser.IndexedLogElement> onResponseHeadersElements,
      List<AccessLogFormatParser.IndexedLogElement> onResponseWriteElements,
      List<AccessLogFormatParser.IndexedLogElement> onLastResponseWriteElements,
      String[] elements
   ) {
      this.onRequestHeadersElements = onRequestHeadersElements;
      this.onResponseHeadersElements = onResponseHeadersElements;
      this.onResponseWriteElements = onResponseWriteElements;
      this.onLastResponseWriteElements = onLastResponseWriteElements;
      this.elements = elements;
   }

   public void reset() {
      this.onRequestHeadersElements.forEach(this::resetIndexedLogElement);
      this.onResponseHeadersElements.forEach(this::resetIndexedLogElement);
      this.onResponseWriteElements.forEach(this::resetIndexedLogElement);
      this.onLastResponseWriteElements.forEach(this::resetIndexedLogElement);
   }

   public void onRequestHeaders(SocketChannel channel, String method, HttpHeaders headers, String uri, String protocol) {
      for(AccessLogFormatParser.IndexedLogElement element : this.onRequestHeadersElements) {
         this.elements[element.index] = element.onRequestHeaders(channel, method, headers, uri, protocol);
      }

   }

   public void onResponseHeaders(ChannelHandlerContext ctx, HttpHeaders headers, String status) {
      for(AccessLogFormatParser.IndexedLogElement element : this.onResponseHeadersElements) {
         this.elements[element.index] = element.onResponseHeaders(ctx, headers, status);
      }

   }

   public void onResponseWrite(int bytesSent) {
      for(AccessLogFormatParser.IndexedLogElement element : this.onResponseWriteElements) {
         element.onResponseWrite(bytesSent);
      }

   }

   public void onLastResponseWrite(int bytesSent) {
      for(AccessLogFormatParser.IndexedLogElement element : this.onLastResponseWriteElements) {
         this.elements[element.index] = element.onLastResponseWrite(bytesSent);
      }

   }

   public void log(Logger accessLogger) {
      if (accessLogger.isInfoEnabled()) {
         StringBuilder b = new StringBuilder(this.elements.length * 5);

         for(int i = 0; i < this.elements.length; ++i) {
            b.append(this.elements[i] == null ? "-" : this.elements[i]);
         }

         accessLogger.info(b.toString());
      }

   }

   private void resetIndexedLogElement(AccessLogFormatParser.IndexedLogElement elt) {
      this.elements[elt.index] = null;
      elt.reset();
   }
}
