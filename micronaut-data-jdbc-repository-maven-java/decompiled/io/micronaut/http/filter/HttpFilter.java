package io.micronaut.http.filter;

import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import org.reactivestreams.Publisher;

public interface HttpFilter extends Ordered {
   Publisher<? extends HttpResponse<?>> doFilter(HttpRequest<?> request, FilterChain chain);
}
