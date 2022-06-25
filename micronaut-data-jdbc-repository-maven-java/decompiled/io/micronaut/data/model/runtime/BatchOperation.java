package io.micronaut.data.model.runtime;

public interface BatchOperation<E> extends EntityOperation<E>, Iterable<E>, PreparedDataOperation<E> {
   default boolean all() {
      return false;
   }
}
