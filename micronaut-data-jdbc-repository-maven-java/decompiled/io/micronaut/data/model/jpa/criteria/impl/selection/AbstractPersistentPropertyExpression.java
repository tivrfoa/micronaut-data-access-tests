package io.micronaut.data.model.jpa.criteria.impl.selection;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.jpa.criteria.IExpression;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitable;

@Internal
public abstract class AbstractPersistentPropertyExpression<P, R> implements IExpression<R>, SelectionVisitable {
   protected final PersistentPropertyPath<P> persistentPropertyPath;

   protected AbstractPersistentPropertyExpression(PersistentPropertyPath<P> persistentPropertyPath) {
      this.persistentPropertyPath = persistentPropertyPath;
   }

   public final PersistentProperty getProperty() {
      return this.persistentPropertyPath.getProperty();
   }
}
