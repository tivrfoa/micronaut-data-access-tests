package io.micronaut.http.server.netty.types;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.netty.channel.ChannelHandlerContext;

@Internal
@Indexed(NettyCustomizableResponseTypeHandler.class)
public interface NettyCustomizableResponseTypeHandler<T> extends Ordered {
   void handle(T object, HttpRequest<?> request, MutableHttpResponse<?> response, ChannelHandlerContext context);

   boolean supports(Class<?> type);
}
