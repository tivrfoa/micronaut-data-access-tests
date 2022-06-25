package io.micronaut.http.client;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.net.URL;

public interface StreamingHttpClientFactory {
   @NonNull
   StreamingHttpClient createStreamingClient(@Nullable URL url);

   @NonNull
   StreamingHttpClient createStreamingClient(@Nullable URL url, @NonNull HttpClientConfiguration configuration);
}
