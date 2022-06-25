package io.micronaut.http.client.filters;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import org.reactivestreams.Publisher;

@Internal
public class ClientServerContextFilter implements HttpClientFilter {
   private final HttpRequest<?> parentRequest;

   public ClientServerContextFilter(HttpRequest<?> parentRequest) {
      this.parentRequest = parentRequest;
   }

   @Override
   public int getOrder() {
      return Integer.MIN_VALUE;
   }

   @Override
   public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
      Publisher<? extends HttpResponse<?>> publisher = chain.proceed(request);
      return new ClientServerRequestTracingPublisher(this.parentRequest, publisher);
   }
}
