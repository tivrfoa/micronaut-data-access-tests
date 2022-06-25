package io.micronaut.http.server.exceptions.response;

import java.util.Optional;

public interface Error {
   default Optional<String> getPath() {
      return Optional.empty();
   }

   String getMessage();

   default Optional<String> getTitle() {
      return Optional.empty();
   }
}
