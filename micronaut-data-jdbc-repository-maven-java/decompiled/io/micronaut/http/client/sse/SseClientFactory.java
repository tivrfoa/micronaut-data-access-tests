package io.micronaut.http.client.sse;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.client.HttpClientConfiguration;
import java.net.URL;

public interface SseClientFactory {
   @NonNull
   SseClient createSseClient(@Nullable URL url);

   @NonNull
   SseClient createSseClient(@Nullable URL url, @NonNull HttpClientConfiguration configuration);
}
