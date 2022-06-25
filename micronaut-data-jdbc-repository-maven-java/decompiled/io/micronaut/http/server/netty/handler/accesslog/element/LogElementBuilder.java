package io.micronaut.http.server.netty.handler.accesslog.element;

import io.micronaut.core.order.Ordered;

public interface LogElementBuilder extends Ordered {
   LogElement build(String token, String param);
}
