package io.micronaut.http.netty.stream;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.reactivestreams.Processor;

public interface WebSocketHttpResponse extends HttpResponse, Processor<WebSocketFrame, WebSocketFrame> {
   WebSocketServerHandshakerFactory handshakerFactory();
}
