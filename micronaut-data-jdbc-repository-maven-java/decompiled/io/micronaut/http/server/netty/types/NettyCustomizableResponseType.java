package io.micronaut.http.server.netty.types;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.server.types.CustomizableResponseType;
import io.netty.channel.ChannelHandlerContext;

@Internal
public interface NettyCustomizableResponseType extends CustomizableResponseType {
   void write(HttpRequest<?> request, MutableHttpResponse<?> response, ChannelHandlerContext context);
}
