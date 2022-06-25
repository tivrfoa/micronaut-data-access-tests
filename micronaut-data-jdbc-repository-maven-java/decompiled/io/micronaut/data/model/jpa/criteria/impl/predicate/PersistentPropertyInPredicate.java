package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitor;
import java.util.Collection;

@Internal
public final class PersistentPropertyInPredicate<T> extends AbstractPersistentPropertyPredicate<T> {
   private final Collection<?> values;

   public PersistentPropertyInPredicate(PersistentPropertyPath<T> persistentPropertyPath, Collection<?> values) {
      super(persistentPropertyPath);
      this.values = values;
   }

   @Override
   public void accept(PredicateVisitor predicateVisitor) {
      predicateVisitor.visit(this);
   }

   public Collection<?> getValues() {
      return this.values;
   }

   public String toString() {
      return "PersistentPropertyInPredicate{persistentPropertyPath=" + this.persistentPropertyPath + ", values=" + this.values + '}';
   }
}
