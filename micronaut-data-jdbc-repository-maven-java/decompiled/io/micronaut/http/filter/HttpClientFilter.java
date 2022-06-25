package io.micronaut.http.filter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import org.reactivestreams.Publisher;

public interface HttpClientFilter extends HttpFilter {
   Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain);

   @Override
   default Publisher<? extends HttpResponse<?>> doFilter(HttpRequest<?> request, FilterChain chain) {
      if (!(request instanceof MutableHttpRequest)) {
         throw new IllegalArgumentException("Passed request must be an instance of " + MutableHttpRequest.class.getName());
      } else if (!(chain instanceof ClientFilterChain)) {
         throw new IllegalArgumentException("Passed chain must be an instance of " + ClientFilterChain.class.getName());
      } else {
         return this.doFilter((MutableHttpRequest<?>)request, (ClientFilterChain)chain);
      }
   }
}
