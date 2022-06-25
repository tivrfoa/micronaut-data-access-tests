package io.micronaut.web.router;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import java.util.Optional;
import java.util.function.Predicate;

public interface StatusRoute extends MethodBasedRoute {
   @Nullable
   Class<?> originatingType();

   HttpStatus status();

   <T> Optional<RouteMatch<T>> match(HttpStatus status);

   <T> Optional<RouteMatch<T>> match(Class originatingClass, HttpStatus status);

   StatusRoute consumes(MediaType... mediaType);

   StatusRoute nest(Runnable nested);

   StatusRoute where(Predicate<HttpRequest<?>> condition);
}
