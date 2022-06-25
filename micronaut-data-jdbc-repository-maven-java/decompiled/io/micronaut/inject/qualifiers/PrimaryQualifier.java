package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.BeanType;
import java.util.stream.Stream;

@Internal
public final class PrimaryQualifier<T> implements Qualifier<T> {
   public static final PrimaryQualifier INSTANCE = new PrimaryQualifier();

   private PrimaryQualifier() {
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      return candidates.filter(candidate -> {
         if (!QualifierUtils.matchType(beanType, candidate)) {
            return false;
         } else if (QualifierUtils.matchAny(beanType, candidate)) {
            return true;
         } else {
            return candidate.isPrimary() || QualifierUtils.matchByCandidateName(candidate, beanType, Qualifier.PRIMARY);
         }
      });
   }

   public String toString() {
      return "@Primary";
   }
}
