package io.micronaut.http.netty.reactive;

import org.reactivestreams.Publisher;

public interface HotObservable<T> extends Publisher<T> {
   void closeIfNoSubscriber();
}
