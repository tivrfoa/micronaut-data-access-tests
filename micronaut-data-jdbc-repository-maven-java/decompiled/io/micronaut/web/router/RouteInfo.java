package io.micronaut.web.router;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.sse.Event;
import io.micronaut.inject.util.KotlinExecutableMethodUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface RouteInfo<R> extends AnnotationMetadataProvider {
   ReturnType<? extends R> getReturnType();

   default Argument<?> getBodyType() {
      ReturnType<? extends R> returnType = this.getReturnType();
      if (returnType.isAsyncOrReactive()) {
         Argument<?> reactiveType = (Argument)returnType.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
         if (HttpResponse.class.isAssignableFrom(reactiveType.getType())) {
            reactiveType = (Argument)reactiveType.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
         }

         return reactiveType;
      } else if (HttpResponse.class.isAssignableFrom(returnType.getType())) {
         Argument<?> responseType = (Argument)returnType.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
         return responseType.isAsyncOrReactive() ? (Argument)responseType.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT) : responseType;
      } else {
         return returnType.asArgument();
      }
   }

   Class<?> getDeclaringType();

   default List<MediaType> getProduces() {
      MediaType[] types = MediaType.of((CharSequence[])this.getAnnotationMetadata().stringValues(Produces.class));
      Optional<Argument<?>> firstTypeVariable = this.getReturnType().getFirstTypeVariable();
      if (firstTypeVariable.isPresent() && Event.class.isAssignableFrom(((Argument)firstTypeVariable.get()).getType())) {
         return Collections.singletonList(MediaType.TEXT_EVENT_STREAM_TYPE);
      } else {
         return ArrayUtils.isNotEmpty(types) ? Collections.unmodifiableList(Arrays.asList(types)) : Route.DEFAULT_PRODUCES;
      }
   }

   default List<MediaType> getConsumes() {
      MediaType[] types = MediaType.of((CharSequence[])this.getAnnotationMetadata().stringValues(Consumes.class));
      return ArrayUtils.isNotEmpty(types) ? Collections.unmodifiableList(Arrays.asList(types)) : Collections.emptyList();
   }

   default boolean isSuspended() {
      return this.getReturnType().isSuspended();
   }

   default boolean isReactive() {
      return this.getReturnType().isReactive();
   }

   default boolean isSingleResult() {
      ReturnType<? extends R> returnType = this.getReturnType();
      return returnType.isSingleResult()
         || this.isReactive() && returnType.getFirstTypeVariable().filter(t -> HttpResponse.class.isAssignableFrom(t.getType())).isPresent()
         || returnType.isAsync()
         || returnType.isSuspended();
   }

   default boolean isSpecifiedSingle() {
      return this.getReturnType().isSpecifiedSingle();
   }

   default boolean isCompletable() {
      return this.getReturnType().isCompletable();
   }

   default boolean isAsync() {
      return this.getReturnType().isAsync();
   }

   default boolean isAsyncOrReactive() {
      return this.getReturnType().isAsyncOrReactive();
   }

   default boolean isVoid() {
      if (this.getReturnType().isVoid()) {
         return true;
      } else {
         return this instanceof MethodBasedRouteMatch && this.isSuspended()
            ? KotlinExecutableMethodUtils.isKotlinFunctionReturnTypeUnit(((MethodBasedRouteMatch)this).getExecutableMethod())
            : false;
      }
   }

   default boolean isErrorRoute() {
      return false;
   }

   @NonNull
   default HttpStatus findStatus(HttpStatus defaultStatus) {
      return (HttpStatus)this.getAnnotationMetadata().enumValue(Status.class, HttpStatus.class).orElse(defaultStatus);
   }

   default boolean isWebSocketRoute() {
      return this.getAnnotationMetadata().hasAnnotation("io.micronaut.websocket.annotation.OnMessage");
   }
}
