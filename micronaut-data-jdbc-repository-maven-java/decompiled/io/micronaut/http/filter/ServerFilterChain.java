package io.micronaut.http.filter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import org.reactivestreams.Publisher;

public interface ServerFilterChain extends FilterChain {
   @Override
   Publisher<MutableHttpResponse<?>> proceed(HttpRequest<?> request);
}
