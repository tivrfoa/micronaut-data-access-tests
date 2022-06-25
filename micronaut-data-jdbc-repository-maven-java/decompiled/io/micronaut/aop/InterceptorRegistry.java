package io.micronaut.aop;

import io.micronaut.context.BeanRegistration;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanConstructor;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.Executable;
import java.util.Collection;

public interface InterceptorRegistry {
   Argument<InterceptorRegistry> ARGUMENT = Argument.of(InterceptorRegistry.class);

   @NonNull
   <T> Interceptor<T, ?>[] resolveInterceptors(
      @NonNull Executable<T, ?> method, @NonNull Collection<BeanRegistration<Interceptor<T, ?>>> interceptors, @NonNull InterceptorKind interceptorKind
   );

   @NonNull
   <T> Interceptor<T, T>[] resolveConstructorInterceptors(
      @NonNull BeanConstructor<T> constructor, @NonNull Collection<BeanRegistration<Interceptor<T, T>>> interceptors
   );
}
