package io.micronaut.http.server.netty.types;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.CollectionUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Internal
public class DefaultCustomizableResponseTypeHandlerRegistry implements NettyCustomizableResponseTypeHandlerRegistry {
   private List<NettyCustomizableResponseTypeHandler> handlers;
   private ConcurrentHashMap<Class<?>, Optional<NettyCustomizableResponseTypeHandler>> handlerCache = new ConcurrentHashMap(5);

   public DefaultCustomizableResponseTypeHandlerRegistry(NettyCustomizableResponseTypeHandler... typeHandlers) {
      this.handlers = Arrays.asList(typeHandlers);
   }

   public DefaultCustomizableResponseTypeHandlerRegistry(List<NettyCustomizableResponseTypeHandler> typeHandlers) {
      this.handlers = CollectionUtils.isNotEmpty(typeHandlers) ? typeHandlers : Collections.emptyList();
   }

   @Override
   public Optional<NettyCustomizableResponseTypeHandler> findTypeHandler(Class<?> type) {
      Optional<NettyCustomizableResponseTypeHandler> foundHandler = (Optional)this.handlerCache.get(type);
      if (foundHandler != null) {
         return foundHandler;
      } else {
         Optional<NettyCustomizableResponseTypeHandler> optionalHandler = Optional.empty();

         for(NettyCustomizableResponseTypeHandler handler : this.handlers) {
            if (handler.supports(type)) {
               optionalHandler = Optional.of(handler);
               break;
            }
         }

         this.handlerCache.put(type, optionalHandler);
         return optionalHandler;
      }
   }
}
