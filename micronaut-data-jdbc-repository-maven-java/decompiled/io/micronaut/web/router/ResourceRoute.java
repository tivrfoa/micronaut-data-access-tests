package io.micronaut.web.router;

import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import java.util.function.Predicate;

public interface ResourceRoute extends Route {
   ResourceRoute consumes(MediaType... mediaTypes);

   ResourceRoute nest(Runnable nested);

   ResourceRoute readOnly(boolean readOnly);

   ResourceRoute exclude(HttpMethod... methods);

   ResourceRoute where(Predicate<HttpRequest<?>> condition);

   ResourceRoute produces(MediaType... mediaType);
}
