package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.BeanType;
import java.util.Optional;
import java.util.stream.Stream;

@Internal
public final class AnyQualifier<T> implements Qualifier<T> {
   public static final AnyQualifier INSTANCE = new AnyQualifier();

   private AnyQualifier() {
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      return candidates;
   }

   @Override
   public boolean contains(Qualifier<T> qualifier) {
      return true;
   }

   @Override
   public <BT extends BeanType<T>> Optional<BT> qualify(Class<T> beanType, Stream<BT> candidates) {
      return candidates.findFirst();
   }

   public String toString() {
      return "@Any";
   }
}
