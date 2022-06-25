package io.micronaut.http.server.netty.jackson;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.json.codec.JsonMediaTypeCodec;

interface JsonViewCodecResolver {
   @NonNull
   JsonMediaTypeCodec resolveJsonViewCodec(@NonNull Class<?> viewClass);
}
