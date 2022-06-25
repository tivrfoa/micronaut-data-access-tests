package io.micronaut.core.type;

public interface ArgumentValue<V> extends Argument<V> {
   V getValue();

   static <T> ArgumentValue<T> create(Argument<T> argument, T value) {
      return new DefaultArgumentValue<>(argument, value);
   }
}
