package io.micronaut.http.netty.stream;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Stream;

public interface Http2Content extends HttpContent {
   Http2Stream stream();
}
