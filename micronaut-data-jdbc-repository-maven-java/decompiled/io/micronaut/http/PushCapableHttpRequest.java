package io.micronaut.http;

import io.micronaut.core.annotation.NonNull;

public interface PushCapableHttpRequest<B> extends HttpRequest<B> {
   boolean isServerPushSupported();

   PushCapableHttpRequest<B> serverPush(@NonNull HttpRequest<?> request);
}
