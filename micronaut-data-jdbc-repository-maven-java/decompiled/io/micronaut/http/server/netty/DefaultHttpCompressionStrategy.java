package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.MediaType;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Internal
@Singleton
class DefaultHttpCompressionStrategy implements HttpCompressionStrategy {
   private final int compressionThreshold;
   private final int compressionLevel;

   @Inject
   DefaultHttpCompressionStrategy(NettyHttpServerConfiguration serverConfiguration) {
      this.compressionThreshold = serverConfiguration.getCompressionThreshold();
      this.compressionLevel = serverConfiguration.getCompressionLevel();
   }

   DefaultHttpCompressionStrategy(int compressionThreshold, int compressionLevel) {
      this.compressionThreshold = compressionThreshold;
      this.compressionLevel = compressionLevel;
   }

   @Override
   public boolean shouldCompress(HttpResponse response) {
      HttpHeaders headers = response.headers();
      String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
      Integer contentLength = headers.getInt(HttpHeaderNames.CONTENT_LENGTH);
      return contentType != null && (contentLength == null || contentLength >= this.compressionThreshold) && MediaType.isTextBased(contentType);
   }

   @Override
   public int getCompressionLevel() {
      return this.compressionLevel;
   }
}
