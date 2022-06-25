package io.micronaut.http.server;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.MethodReference;
import io.micronaut.web.router.RouteInfo;
import java.lang.reflect.Method;

class ExecutableRouteInfo<T> implements RouteInfo<Object>, MethodReference<T, Object> {
   private final ExecutableMethod<T, Object> method;
   private final boolean errorRoute;

   ExecutableRouteInfo(ExecutableMethod<T, Object> method, boolean errorRoute) {
      this.method = method;
      this.errorRoute = errorRoute;
   }

   @Override
   public Argument<?>[] getArguments() {
      return this.method.getArguments();
   }

   @Override
   public Method getTargetMethod() {
      return this.method.getTargetMethod();
   }

   @Override
   public ReturnType<Object> getReturnType() {
      return this.method.getReturnType();
   }

   @Override
   public Class<T> getDeclaringType() {
      return this.method.getDeclaringType();
   }

   @Override
   public String getMethodName() {
      return this.method.getMethodName();
   }

   @Override
   public boolean isErrorRoute() {
      return this.errorRoute;
   }

   @NonNull
   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.method.getAnnotationMetadata();
   }
}
