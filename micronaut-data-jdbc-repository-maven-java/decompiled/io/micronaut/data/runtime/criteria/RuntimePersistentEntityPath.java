package io.micronaut.data.runtime.criteria;

import io.micronaut.data.model.Association;
import io.micronaut.data.model.jpa.criteria.PersistentAssociationPath;
import io.micronaut.data.model.jpa.criteria.PersistentEntityPath;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

interface RuntimePersistentEntityPath<T> extends PersistentEntityPath<T> {
   RuntimePersistentEntity<T> getPersistentEntity();

   @Override
   default <Y> PersistentPropertyPath<Y> get(String attributeName) {
      RuntimePersistentProperty<?> property = this.getPersistentEntity().getPropertyByName(attributeName);
      if (property == null) {
         throw new IllegalStateException("Cannot query entity [" + this.getPersistentEntity().getSimpleName() + "] on non-existent property: " + attributeName);
      } else if (this instanceof PersistentAssociationPath) {
         PersistentAssociationPath<?, ?> associationPath = (PersistentAssociationPath)this;
         List<Association> associations = associationPath.getAssociations();
         List<Association> newAssociations = new ArrayList(associations.size() + 1);
         newAssociations.addAll(associations);
         newAssociations.add(associationPath.getAssociation());
         return new RuntimePersistentPropertyPathImpl<>(this, newAssociations, property);
      } else {
         return new RuntimePersistentPropertyPathImpl<>(this, Collections.emptyList(), property);
      }
   }
}
