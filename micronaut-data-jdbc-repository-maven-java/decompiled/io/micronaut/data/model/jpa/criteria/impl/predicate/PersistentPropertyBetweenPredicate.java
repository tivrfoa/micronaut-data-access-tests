package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitor;

@Internal
public final class PersistentPropertyBetweenPredicate<T> extends AbstractPersistentPropertyPredicate<T> {
   private final Object from;
   private final Object to;

   public PersistentPropertyBetweenPredicate(PersistentPropertyPath<T> persistentPropertyPath, Object from, Object to) {
      super(persistentPropertyPath);
      this.from = from;
      this.to = to;
   }

   public Object getFrom() {
      return this.from;
   }

   public Object getTo() {
      return this.to;
   }

   @Override
   public void accept(PredicateVisitor predicateVisitor) {
      predicateVisitor.visit(this);
   }

   public String toString() {
      return "PersistentPropertyBetweenPredicate{persistentPropertyPath=" + this.persistentPropertyPath + ", from=" + this.from + ", to=" + this.to + '}';
   }
}
