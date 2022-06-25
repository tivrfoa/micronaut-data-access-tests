package io.micronaut.web.router;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public interface Route {
   List<MediaType> DEFAULT_PRODUCES = Collections.singletonList(MediaType.APPLICATION_JSON_TYPE);

   Route consumes(MediaType... mediaType);

   Route produces(MediaType... mediaType);

   Route consumesAll();

   Route nest(Runnable nested);

   Route where(Predicate<HttpRequest<?>> condition);

   Route body(String argument);

   Route body(Argument<?> argument);

   default List<MediaType> getProduces() {
      return DEFAULT_PRODUCES;
   }

   default List<MediaType> getConsumes() {
      return Collections.emptyList();
   }
}
