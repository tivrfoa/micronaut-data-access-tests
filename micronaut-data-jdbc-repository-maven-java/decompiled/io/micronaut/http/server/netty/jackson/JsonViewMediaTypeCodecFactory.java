package io.micronaut.http.server.netty.jackson;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.json.JsonConfiguration;
import io.micronaut.json.codec.JsonMediaTypeCodec;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Requirements({@Requires(
   beans = {JsonConfiguration.class}
), @Requires(
   property = "jackson.json-view.enabled"
)})
@Singleton
@Primary
class JsonViewMediaTypeCodecFactory implements JsonViewCodecResolver {
   private final JsonMediaTypeCodec jsonCodec;
   private final Map<Class<?>, JsonMediaTypeCodec> jsonViewCodecs = new ConcurrentHashMap(5);

   JsonViewMediaTypeCodecFactory(JsonMediaTypeCodec jsonCodec) {
      this.jsonCodec = jsonCodec;
   }

   @NonNull
   @Override
   public JsonMediaTypeCodec resolveJsonViewCodec(@NonNull Class<?> viewClass) {
      ArgumentUtils.requireNonNull("viewClass", viewClass);
      JsonMediaTypeCodec codec = (JsonMediaTypeCodec)this.jsonViewCodecs.get(viewClass);
      if (codec == null) {
         codec = (JsonMediaTypeCodec)this.jsonCodec.cloneWithViewClass(viewClass);
         this.jsonViewCodecs.put(viewClass, codec);
      }

      return codec;
   }
}
