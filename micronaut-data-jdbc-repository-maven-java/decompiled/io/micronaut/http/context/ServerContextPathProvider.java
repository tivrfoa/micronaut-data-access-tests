package io.micronaut.http.context;

import io.micronaut.core.annotation.Nullable;

public interface ServerContextPathProvider {
   @Nullable
   String getContextPath();
}
