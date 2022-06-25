package io.micronaut.data.model.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.jpa.criteria.impl.CriteriaUtils;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Bindable;
import java.util.ArrayList;
import java.util.List;

public interface PersistentAssociationPath<OwnerType, AssociatedEntityType>
   extends PersistentEntityJoin<OwnerType, AssociatedEntityType>,
   PersistentPropertyPath<AssociatedEntityType> {
   @NonNull
   Association getProperty();

   @NonNull
   Association getAssociation();

   @Nullable
   Join.Type getAssociationJoinType();

   void setAssociationJoinType(@Nullable Join.Type type);

   void setAlias(String alias);

   @NonNull
   default List<Association> asPath() {
      List<Association> associations = this.getAssociations();
      List<Association> newAssociations = new ArrayList(associations.size() + 1);
      newAssociations.addAll(associations);
      newAssociations.add(this.getAssociation());
      return newAssociations;
   }

   @NonNull
   default jakarta.persistence.criteria.Join<OwnerType, AssociatedEntityType> on(Expression<Boolean> restriction) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   default jakarta.persistence.criteria.Join<OwnerType, AssociatedEntityType> on(Predicate... restrictions) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @Nullable
   default Predicate getOn() {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   default Attribute<? super OwnerType, ?> getAttribute() {
      throw CriteriaUtils.notSupportedOperation();
   }

   @Nullable
   default From<?, OwnerType> getParent() {
      return null;
   }

   @NonNull
   default JoinType getJoinType() {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   default Bindable<AssociatedEntityType> getModel() {
      throw CriteriaUtils.notSupportedOperation();
   }
}
