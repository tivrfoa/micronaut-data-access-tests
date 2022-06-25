package io.micronaut.websocket.interceptor;

import io.micronaut.websocket.WebSocketSession;

public interface WebSocketSessionAware {
   void setWebSocketSession(WebSocketSession session);
}
