package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.BeanType;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Internal
final class CompositeQualifier<T> implements Qualifier<T> {
   private final Qualifier[] qualifiers;

   CompositeQualifier(Qualifier... qualifiers) {
      this.qualifiers = qualifiers;
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      Stream<BT> reduced = candidates;

      for(Qualifier qualifier : this.qualifiers) {
         reduced = qualifier.reduce(beanType, reduced);
      }

      return reduced;
   }

   public Qualifier[] getQualifiers() {
      return this.qualifiers;
   }

   @Override
   public boolean contains(Qualifier<T> qualifier) {
      if (qualifier instanceof CompositeQualifier) {
         for(Qualifier q : ((CompositeQualifier)qualifier).qualifiers) {
            if (!this.contains(q)) {
               return false;
            }
         }

         return true;
      } else {
         for(Qualifier q : this.qualifiers) {
            if (q.contains(qualifier)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CompositeQualifier<?> that = (CompositeQualifier)o;
         return Arrays.equals(this.qualifiers, that.qualifiers);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.qualifiers);
   }

   public String toString() {
      return (String)Arrays.stream(this.qualifiers).map(Object::toString).collect(Collectors.joining(" and "));
   }
}
