package io.micronaut.core.util.functional;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {
   R apply(T t) throws E;

   default <V> ThrowingFunction<V, R, E> compose(Function<? super V, ? extends T> before) {
      Objects.requireNonNull(before);
      return v -> this.apply((T)before.apply(v));
   }

   default <V> ThrowingFunction<T, V, E> andThen(Function<? super R, ? extends V> after) {
      Objects.requireNonNull(after);
      return t -> (V)after.apply(this.apply(t));
   }

   static <T, E extends Throwable> ThrowingFunction<T, T, E> identity() {
      return t -> t;
   }
}
