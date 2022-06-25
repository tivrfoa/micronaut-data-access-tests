package io.micronaut.http.server.netty;

public interface HttpContentSubscriberFactory {
   HttpContentProcessor build(NettyHttpRequest request);
}
