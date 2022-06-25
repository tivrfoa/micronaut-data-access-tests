package io.micronaut.http.client;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import java.net.URL;
import org.reactivestreams.Publisher;

public interface ProxyHttpClient {
   Publisher<MutableHttpResponse<?>> proxy(@NonNull HttpRequest<?> request);

   default Publisher<MutableHttpResponse<?>> proxy(@NonNull HttpRequest<?> request, @NonNull ProxyRequestOptions options) {
      if (options.equals(ProxyRequestOptions.getDefault())) {
         return this.proxy(request);
      } else {
         throw new UnsupportedOperationException("Not implemented");
      }
   }

   static ProxyHttpClient create(@Nullable URL url) {
      return ProxyHttpClientFactoryResolver.getFactory().createProxyClient(url);
   }

   static ProxyHttpClient create(@Nullable URL url, @NonNull HttpClientConfiguration configuration) {
      return ProxyHttpClientFactoryResolver.getFactory().createProxyClient(url, configuration);
   }
}
