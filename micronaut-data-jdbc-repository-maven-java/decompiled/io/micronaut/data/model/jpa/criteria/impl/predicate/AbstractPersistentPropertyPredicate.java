package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitable;

@Internal
public abstract class AbstractPersistentPropertyPredicate<T> extends AbstractPredicate implements PredicateVisitable {
   protected final PersistentPropertyPath<T> persistentPropertyPath;

   public AbstractPersistentPropertyPredicate(PersistentPropertyPath<T> persistentPropertyPath) {
      this.persistentPropertyPath = persistentPropertyPath;
   }

   public final PersistentPropertyPath<T> getPropertyPath() {
      return this.persistentPropertyPath;
   }

   public final PersistentProperty getProperty() {
      return this.persistentPropertyPath.getProperty();
   }
}
