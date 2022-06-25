package io.micronaut.websocket.bind;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpRequest;
import io.micronaut.websocket.WebSocketSession;

@Internal
public class WebSocketState {
   private final WebSocketSession session;
   private final HttpRequest<?> originatingRequest;

   public WebSocketState(WebSocketSession session, HttpRequest<?> originatingRequest) {
      this.session = session;
      this.originatingRequest = originatingRequest;
   }

   public WebSocketSession getSession() {
      return this.session;
   }

   public HttpRequest<?> getOriginatingRequest() {
      return this.originatingRequest;
   }
}
