package io.micronaut.management.endpoint.info;

import org.reactivestreams.Publisher;

public interface InfoAggregator<T> {
   Publisher<T> aggregate(InfoSource[] sources);
}
