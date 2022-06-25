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
import java.util.function.Function;

public class BasicObjectRouteMatch implements RouteMatch<Object> {
   private final Object object;

   public BasicObjectRouteMatch(Object object) {
      this.object = object;
   }

   @Override
   public Class<?> getDeclaringType() {
      return this.object.getClass();
   }

   @Override
   public Map<String, Object> getVariableValues() {
      return Collections.emptyMap();
   }

   @Override
   public Object execute(Map<String, Object> argumentValues) {
      return this.object;
   }

   @Override
   public RouteMatch<Object> fulfill(Map<String, Object> argumentValues) {
      return this;
   }

   @Override
   public RouteMatch<Object> decorate(Function<RouteMatch<Object>, Object> executor) {
      return new BasicObjectRouteMatch(executor.apply(this));
   }

   @Override
   public Optional<Argument<?>> getRequiredInput(String name) {
      return Optional.empty();
   }

   @Override
   public Optional<Argument<?>> getBodyArgument() {
      return Optional.empty();
   }

   @Override
   public List<MediaType> getProduces() {
      return Collections.emptyList();
   }

   @Override
   public ReturnType<?> getReturnType() {
      return ReturnType.of(this.object.getClass());
   }

   @Override
   public boolean doesConsume(@Nullable MediaType contentType) {
      return true;
   }

   @Override
   public boolean doesProduce(@Nullable Collection<MediaType> acceptableTypes) {
      return true;
   }

   @Override
   public boolean doesProduce(@Nullable MediaType acceptableType) {
      return true;
   }

   public boolean test(HttpRequest httpRequest) {
      return true;
   }
}
