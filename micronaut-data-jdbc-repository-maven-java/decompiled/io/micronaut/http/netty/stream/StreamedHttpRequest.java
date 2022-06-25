package io.micronaut.http.netty.stream;

import io.netty.handler.codec.http.HttpRequest;

public interface StreamedHttpRequest extends HttpRequest, StreamedHttpMessage {
   default void closeIfNoSubscriber() {
   }

   default boolean isConsumed() {
      return false;
   }
}
