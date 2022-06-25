package io.micronaut.http.client;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.net.URL;

public interface ProxyHttpClientFactory {
   @NonNull
   ProxyHttpClient createProxyClient(@Nullable URL url);

   @NonNull
   ProxyHttpClient createProxyClient(@Nullable URL url, @NonNull HttpClientConfiguration configuration);
}
