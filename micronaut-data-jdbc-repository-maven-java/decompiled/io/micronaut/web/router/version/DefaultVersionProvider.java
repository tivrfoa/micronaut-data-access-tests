package io.micronaut.web.router.version;

import io.micronaut.core.annotation.NonNull;

public interface DefaultVersionProvider {
   @NonNull
   String resolveDefaultVersion();
}
