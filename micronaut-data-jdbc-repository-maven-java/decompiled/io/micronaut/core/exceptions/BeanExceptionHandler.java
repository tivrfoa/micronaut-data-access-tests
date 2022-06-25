package io.micronaut.core.exceptions;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface BeanExceptionHandler<T, E extends Throwable> extends BiConsumer<T, E> {
   void handle(@Nullable T bean, @NonNull E throwable);

   default void accept(@Nullable T bean, @NonNull E throwable) {
      this.handle(bean, throwable);
   }
}
