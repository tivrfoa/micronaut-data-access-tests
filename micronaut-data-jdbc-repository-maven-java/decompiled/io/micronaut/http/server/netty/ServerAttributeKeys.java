package io.micronaut.http.server.netty;

import io.micronaut.http.netty.NettyMutableHttpResponse;
import io.netty.util.AttributeKey;

final class ServerAttributeKeys {
   static final AttributeKey<NettyMutableHttpResponse> RESPONSE_KEY = AttributeKey.valueOf(NettyMutableHttpResponse.class.getSimpleName());
   static final AttributeKey<NettyHttpRequest> REQUEST_KEY = AttributeKey.valueOf(NettyHttpRequest.class.getSimpleName());
}
