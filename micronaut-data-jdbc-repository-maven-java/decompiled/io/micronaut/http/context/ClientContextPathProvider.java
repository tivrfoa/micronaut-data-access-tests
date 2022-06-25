package io.micronaut.http.context;

import java.util.Optional;

public interface ClientContextPathProvider {
   Optional<String> getContextPath();
}
