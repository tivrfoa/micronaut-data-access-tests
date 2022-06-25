package io.micronaut.http.uri;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import java.net.URI;
import java.util.Map;

public interface UriBuilder {
   @NonNull
   UriBuilder fragment(@Nullable String fragment);

   @NonNull
   UriBuilder scheme(@Nullable String scheme);

   @NonNull
   UriBuilder userInfo(@Nullable String userInfo);

   @NonNull
   UriBuilder host(@Nullable String host);

   @NonNull
   UriBuilder port(int port);

   @NonNull
   UriBuilder path(@Nullable String path);

   @NonNull
   UriBuilder replacePath(@Nullable String path);

   @NonNull
   UriBuilder queryParam(String name, Object... values);

   @NonNull
   UriBuilder replaceQueryParam(String name, Object... values);

   @NonNull
   URI build();

   @NonNull
   URI expand(Map<String, ? super Object> values);

   @NonNull
   static UriBuilder of(@NonNull URI uri) {
      ArgumentUtils.requireNonNull("uri", uri);
      return new DefaultUriBuilder(uri);
   }

   @NonNull
   static UriBuilder of(@NonNull CharSequence uri) {
      ArgumentUtils.requireNonNull("uri", uri);
      return new DefaultUriBuilder(uri);
   }
}
