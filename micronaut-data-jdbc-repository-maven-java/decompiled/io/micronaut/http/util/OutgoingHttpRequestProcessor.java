package io.micronaut.http.util;

import io.micronaut.http.HttpRequest;

public interface OutgoingHttpRequestProcessor {
   boolean shouldProcessRequest(OutgointRequestProcessorMatcher matcher, HttpRequest<?> request);
}
