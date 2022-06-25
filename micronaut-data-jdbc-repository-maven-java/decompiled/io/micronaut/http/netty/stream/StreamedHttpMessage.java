package io.micronaut.http.netty.stream;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMessage;
import org.reactivestreams.Publisher;

public interface StreamedHttpMessage extends HttpMessage, Publisher<HttpContent> {
}
