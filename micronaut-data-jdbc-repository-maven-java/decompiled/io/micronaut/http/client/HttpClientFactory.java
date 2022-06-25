package io.micronaut.http.client;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.net.URL;

public interface HttpClientFactory {
   @NonNull
   HttpClient createClient(@Nullable URL url);

   @NonNull
   HttpClient createClient(@Nullable URL url, @NonNull HttpClientConfiguration configuration);
}
