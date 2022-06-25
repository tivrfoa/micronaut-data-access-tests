package io.micronaut.http.netty;

import io.micronaut.http.netty.stream.Http2Content;
import io.netty.handler.codec.http.LastHttpContent;

public interface LastHttp2Content extends Http2Content, LastHttpContent {
}
