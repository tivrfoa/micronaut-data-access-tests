package io.micronaut.http.uri;

import java.net.URI;
import java.util.Optional;

public interface UriMatcher {
   default Optional<? extends UriMatchInfo> match(URI uri) {
      return this.match(uri.toString());
   }

   Optional<? extends UriMatchInfo> match(String uri);
}
