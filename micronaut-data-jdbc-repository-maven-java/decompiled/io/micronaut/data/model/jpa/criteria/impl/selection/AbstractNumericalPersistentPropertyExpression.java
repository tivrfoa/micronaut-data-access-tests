package io.micronaut.data.model.jpa.criteria.impl.selection;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;

@Internal
public abstract class AbstractNumericalPersistentPropertyExpression<N extends Number> extends AbstractPersistentPropertyExpression<N, N> {
   protected AbstractNumericalPersistentPropertyExpression(PersistentPropertyPath<N> persistentPropertyPath) {
      super(persistentPropertyPath);
   }

   public Class<? extends N> getJavaType() {
      return this.persistentPropertyPath.getJavaType();
   }
}
