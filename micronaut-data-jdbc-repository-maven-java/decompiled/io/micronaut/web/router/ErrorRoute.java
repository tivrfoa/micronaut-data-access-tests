package io.micronaut.web.router;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import java.util.Optional;
import java.util.function.Predicate;

public interface ErrorRoute extends MethodBasedRoute {
   Class<?> originatingType();

   Class<? extends Throwable> exceptionType();

   <T> Optional<RouteMatch<T>> match(Throwable exception);

   <T> Optional<RouteMatch<T>> match(Class originatingClass, Throwable exception);

   ErrorRoute consumes(MediaType... mediaType);

   ErrorRoute nest(Runnable nested);

   ErrorRoute where(Predicate<HttpRequest<?>> condition);

   ErrorRoute produces(MediaType... mediaType);
}
