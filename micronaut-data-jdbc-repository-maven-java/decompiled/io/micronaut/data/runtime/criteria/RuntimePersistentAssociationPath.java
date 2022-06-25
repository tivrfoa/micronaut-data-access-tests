package io.micronaut.data.runtime.criteria;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.jpa.criteria.PersistentAssociationPath;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitor;
import io.micronaut.data.model.runtime.RuntimeAssociation;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import jakarta.persistence.criteria.Path;
import java.util.ArrayList;
import java.util.List;

final class RuntimePersistentAssociationPath<Owner, E>
   extends AbstractRuntimePersistentEntityJoinSupport<Owner, E>
   implements RuntimePersistentEntityPath<E>,
   PersistentAssociationPath<Owner, E> {
   private final Path<?> parentPath;
   private final RuntimeAssociation<Owner> association;
   private final List<Association> associations;
   private Join.Type associationJoinType;
   @Nullable
   private String alias;

   RuntimePersistentAssociationPath(
      Path<?> parentPath, RuntimeAssociation<Owner> association, List<Association> associations, Join.Type associationJoinType, @Nullable String alias
   ) {
      this.parentPath = parentPath;
      this.association = association;
      this.associations = associations;
      this.associationJoinType = associationJoinType;
      this.alias = alias;
   }

   @Override
   public Join.Type getAssociationJoinType() {
      return this.associationJoinType;
   }

   @Override
   public void setAssociationJoinType(Join.Type type) {
      this.associationJoinType = type;
   }

   @Nullable
   @Override
   public String getAlias() {
      return this.alias;
   }

   @Override
   public void setAlias(String alias) {
      this.alias = alias;
   }

   @Override
   public void accept(SelectionVisitor selectionVisitor) {
      selectionVisitor.visit(this);
   }

   @Override
   public Path<?> getParentPath() {
      return this.parentPath;
   }

   public RuntimeAssociation<Owner> getProperty() {
      return this.association;
   }

   @Override
   public Association getAssociation() {
      return this.association;
   }

   @Override
   public List<Association> getAssociations() {
      return this.associations;
   }

   @Override
   public RuntimePersistentEntity<E> getPersistentEntity() {
      return this.association.getAssociatedEntity();
   }

   @Override
   protected List<Association> getCurrentPath() {
      return associated(this.getAssociations(), this.association);
   }

   private static List<Association> associated(List<Association> associations, Association association) {
      List<Association> newAssociations = new ArrayList(associations.size() + 1);
      newAssociations.addAll(associations);
      newAssociations.add(association);
      return newAssociations;
   }

   public String toString() {
      return "RuntimePersistentAssociationPath{parentPath="
         + this.parentPath
         + ", association="
         + this.association
         + ", associations="
         + this.associations
         + '}';
   }
}
