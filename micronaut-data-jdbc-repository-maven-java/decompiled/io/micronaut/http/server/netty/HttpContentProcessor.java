package io.micronaut.http.server.netty;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.util.Toggleable;
import io.netty.buffer.ByteBufHolder;
import org.reactivestreams.Subscriber;

public interface HttpContentProcessor<T> extends Publishers.MicronautPublisher<T>, Subscriber<ByteBufHolder>, Toggleable {
}
