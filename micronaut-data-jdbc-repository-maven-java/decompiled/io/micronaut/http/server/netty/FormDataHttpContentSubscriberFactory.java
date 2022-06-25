package io.micronaut.http.server.netty;

import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import jakarta.inject.Singleton;

@Consumes({"application/x-www-form-urlencoded", "multipart/form-data"})
@Singleton
public class FormDataHttpContentSubscriberFactory implements HttpContentSubscriberFactory {
   private final NettyHttpServerConfiguration configuration;

   public FormDataHttpContentSubscriberFactory(NettyHttpServerConfiguration configuration) {
      this.configuration = configuration;
   }

   @Override
   public HttpContentProcessor build(NettyHttpRequest request) {
      return new FormDataHttpContentProcessor(request, this.configuration);
   }
}
