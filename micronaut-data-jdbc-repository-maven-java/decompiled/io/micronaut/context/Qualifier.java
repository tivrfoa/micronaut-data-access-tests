package io.micronaut.context;

import io.micronaut.context.annotation.Primary;
import io.micronaut.inject.BeanType;
import java.util.Optional;
import java.util.stream.Stream;

public interface Qualifier<T> {
   String PRIMARY = Primary.class.getName();

   <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates);

   default boolean contains(Qualifier<T> qualifier) {
      return this.equals(qualifier);
   }

   default <BT extends BeanType<T>> Optional<BT> qualify(Class<T> beanType, Stream<BT> candidates) {
      return this.reduce(beanType, candidates).findFirst();
   }
}
