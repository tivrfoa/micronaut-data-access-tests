package io.micronaut.http.server.netty.types;

import io.micronaut.core.annotation.Internal;
import java.util.Optional;

@Internal
public interface NettyCustomizableResponseTypeHandlerRegistry {
   Optional<NettyCustomizableResponseTypeHandler> findTypeHandler(Class<?> type);
}
