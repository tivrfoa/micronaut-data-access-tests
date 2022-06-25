package io.micronaut.http.filter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import org.reactivestreams.Publisher;

public interface FilterChain {
   Publisher<? extends HttpResponse<?>> proceed(HttpRequest<?> request);
}
