package io.micronaut.http.server.util;

import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;

@DefaultImplementation(DefaultHttpHostResolver.class)
public interface HttpHostResolver {
   @NonNull
   String resolve(@Nullable HttpRequest request);
}
