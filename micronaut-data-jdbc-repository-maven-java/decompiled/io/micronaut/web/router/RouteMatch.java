package io.micronaut.web.router;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

public interface RouteMatch<R> extends Callable<R>, Predicate<HttpRequest>, RouteInfo<R> {
   Map<String, Object> getVariableValues();

   R execute(Map<String, Object> argumentValues);

   RouteMatch<R> fulfill(Map<String, Object> argumentValues);

   RouteMatch<R> decorate(Function<RouteMatch<R>, R> executor);

   Optional<Argument<?>> getRequiredInput(String name);

   Optional<Argument<?>> getBodyArgument();

   @Override
   List<MediaType> getProduces();

   default Collection<Argument> getRequiredArguments() {
      return Collections.emptyList();
   }

   @Override
   ReturnType<? extends R> getReturnType();

   default R execute() {
      return this.execute(Collections.emptyMap());
   }

   default R call() throws Exception {
      return this.execute();
   }

   default boolean isExecutable() {
      return this.getRequiredArguments().isEmpty();
   }

   default boolean isRequiredInput(String name) {
      return this.getRequiredInput(name).isPresent();
   }

   boolean doesConsume(@Nullable MediaType contentType);

   boolean doesProduce(@Nullable Collection<MediaType> acceptableTypes);

   boolean doesProduce(@Nullable MediaType acceptableType);

   default boolean explicitlyConsumes(@Nullable MediaType contentType) {
      return false;
   }

   default boolean explicitlyProduces(@Nullable MediaType contentType) {
      return false;
   }

   default boolean isSatisfied(String name) {
      Object val = this.getVariableValues().get(name);
      return val != null && !(val instanceof UnresolvedArgument);
   }
}
