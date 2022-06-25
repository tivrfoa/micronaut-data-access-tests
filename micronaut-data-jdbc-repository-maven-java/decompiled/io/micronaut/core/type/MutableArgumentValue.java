package io.micronaut.core.type;

public interface MutableArgumentValue<V> extends ArgumentValue<V> {
   void setValue(V value);

   static <T> MutableArgumentValue<T> create(Argument<T> argument, T value) {
      return new DefaultMutableArgumentValue<>(argument, value);
   }
}
