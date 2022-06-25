package io.micronaut.http.netty.stream;

import io.netty.handler.codec.http.HttpResponse;

public interface StreamedHttpResponse extends HttpResponse, StreamedHttpMessage {
}
