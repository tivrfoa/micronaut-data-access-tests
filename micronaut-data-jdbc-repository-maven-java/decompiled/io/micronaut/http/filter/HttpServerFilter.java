package io.micronaut.http.filter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import org.reactivestreams.Publisher;

public interface HttpServerFilter extends HttpFilter {
   Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain);

   @Override
   default Publisher<? extends HttpResponse<?>> doFilter(HttpRequest<?> request, FilterChain chain) {
      if (!(chain instanceof ServerFilterChain)) {
         throw new IllegalArgumentException("Passed FilterChain must be an instance of ServerFilterChain");
      } else {
         return this.doFilter(request, (ServerFilterChain)chain);
      }
   }
}
