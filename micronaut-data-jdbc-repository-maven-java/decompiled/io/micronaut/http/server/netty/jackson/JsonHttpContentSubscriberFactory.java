package io.micronaut.http.server.netty.jackson;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.netty.HttpContentProcessor;
import io.micronaut.http.server.netty.HttpContentSubscriberFactory;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.json.JsonMapper;
import jakarta.inject.Singleton;

@Consumes({"application/x-json-stream", "application/json"})
@Singleton
@Internal
public class JsonHttpContentSubscriberFactory implements HttpContentSubscriberFactory {
   private final HttpServerConfiguration httpServerConfiguration;
   private final JsonMapper jsonMapper;

   public JsonHttpContentSubscriberFactory(JsonMapper jsonMapper, HttpServerConfiguration httpServerConfiguration) {
      this.httpServerConfiguration = httpServerConfiguration;
      this.jsonMapper = jsonMapper;
   }

   @Override
   public HttpContentProcessor build(NettyHttpRequest request) {
      return new JsonContentProcessor(request, this.httpServerConfiguration, this.jsonMapper);
   }
}
