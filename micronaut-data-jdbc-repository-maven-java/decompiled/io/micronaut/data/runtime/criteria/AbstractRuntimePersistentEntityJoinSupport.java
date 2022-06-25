package io.micronaut.data.runtime.criteria;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.jpa.criteria.PersistentAssociationPath;
import io.micronaut.data.model.jpa.criteria.impl.AbstractPersistentEntityJoinSupport;
import io.micronaut.data.model.runtime.RuntimeAssociation;
import java.util.List;

abstract class AbstractRuntimePersistentEntityJoinSupport<T, J> extends AbstractPersistentEntityJoinSupport<T, J> {
   protected abstract List<Association> getCurrentPath();

   @Override
   protected <X, Y> PersistentAssociationPath<X, Y> createJoinAssociation(Association association, Join.Type associationJoinType, String alias) {
      return new RuntimePersistentAssociationPath<>(this, (RuntimeAssociation<X>)association, this.getCurrentPath(), associationJoinType, alias);
   }
}
