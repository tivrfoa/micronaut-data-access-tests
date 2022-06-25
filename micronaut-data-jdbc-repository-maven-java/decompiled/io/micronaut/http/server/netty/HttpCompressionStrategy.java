package io.micronaut.http.server.netty;

import io.netty.handler.codec.http.HttpResponse;

public interface HttpCompressionStrategy {
   boolean shouldCompress(HttpResponse response);

   default int getCompressionLevel() {
      return 6;
   }
}
