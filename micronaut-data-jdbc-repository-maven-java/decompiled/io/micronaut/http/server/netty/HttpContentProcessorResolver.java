package io.micronaut.http.server.netty;

import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.web.router.RouteMatch;

@DefaultImplementation(DefaultHttpContentProcessorResolver.class)
@Internal
public interface HttpContentProcessorResolver {
   @NonNull
   HttpContentProcessor<?> resolve(@NonNull NettyHttpRequest<?> request, @NonNull RouteMatch<?> route);

   @NonNull
   HttpContentProcessor<?> resolve(@NonNull NettyHttpRequest<?> request, @NonNull Argument<?> bodyType);

   @NonNull
   HttpContentProcessor<?> resolve(@NonNull NettyHttpRequest<?> request);
}
