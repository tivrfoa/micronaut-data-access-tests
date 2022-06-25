package io.micronaut.http.filter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import org.reactivestreams.Publisher;

public interface ClientFilterChain extends FilterChain {
   Publisher<? extends HttpResponse<?>> proceed(MutableHttpRequest<?> request);

   @Override
   default Publisher<? extends HttpResponse<?>> proceed(HttpRequest<?> request) {
      if (!(request instanceof MutableHttpRequest)) {
         throw new IllegalArgumentException("A MutableHttpRequest is required");
      } else {
         return this.proceed((MutableHttpRequest<?>)request);
      }
   }
}
