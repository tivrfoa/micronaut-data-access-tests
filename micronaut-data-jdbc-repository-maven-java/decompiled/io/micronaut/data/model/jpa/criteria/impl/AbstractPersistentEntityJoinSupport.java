package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.jpa.criteria.PersistentAssociationPath;
import io.micronaut.data.model.jpa.criteria.PersistentEntityFrom;
import jakarta.persistence.criteria.CollectionJoin;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.metamodel.CollectionAttribute;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.MapAttribute;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Internal
public abstract class AbstractPersistentEntityJoinSupport<J, E> implements PersistentEntityFrom<J, E>, SelectionVisitable {
   protected final Map<String, PersistentAssociationPath> joins = new LinkedHashMap();

   @Override
   public abstract PersistentEntity getPersistentEntity();

   protected abstract <X, Y> PersistentAssociationPath<X, Y> createJoinAssociation(
      @NonNull Association association, @Nullable Join.Type type, @Nullable String alias
   );

   public Path<?> getParentPath() {
      return null;
   }

   public <X, Y> PersistentAssociationPath<X, Y> join(String attributeName) {
      return this.addJoin(attributeName, null, null);
   }

   public <X, Y> PersistentAssociationPath<X, Y> join(String attributeName, Join.Type type) {
      return this.addJoin(attributeName, (Join.Type)Objects.requireNonNull(type), null);
   }

   public <X, Y> PersistentAssociationPath<X, Y> join(String attributeName, Join.Type type, String alias) {
      return this.addJoin(attributeName, (Join.Type)Objects.requireNonNull(type), (String)Objects.requireNonNull(alias));
   }

   private <X, Y> PersistentAssociationPath<X, Y> addJoin(String attributeName, Join.Type type, String alias) {
      PersistentProperty persistentProperty = this.getPersistentEntity().getPropertyByName(attributeName);
      if (!(persistentProperty instanceof Association)) {
         throw new IllegalStateException("Expected an association for attribute name: " + attributeName);
      } else {
         PersistentAssociationPath path = (PersistentAssociationPath)this.joins
            .computeIfAbsent(attributeName, a -> this.createJoinAssociation((Association)persistentProperty, type, alias));
         if (type != null && type != Join.Type.DEFAULT) {
            path.setAssociationJoinType(type);
         }

         if (alias != null) {
            path.setAlias(alias);
         }

         return path;
      }
   }

   public Set<jakarta.persistence.criteria.Join<E, ?>> getJoins() {
      return new HashSet(this.joins.values());
   }

   public boolean isCorrelated() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public From<J, E> getCorrelationParent() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> jakarta.persistence.criteria.Join<E, Y> join(SingularAttribute<? super E, Y> attribute) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> jakarta.persistence.criteria.Join<E, Y> join(SingularAttribute<? super E, Y> attribute, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> CollectionJoin<E, Y> join(CollectionAttribute<? super E, Y> collection) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> SetJoin<E, Y> join(SetAttribute<? super E, Y> set) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> ListJoin<E, Y> join(ListAttribute<? super E, Y> list) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <K, V> MapJoin<E, K, V> join(MapAttribute<? super E, K, V> map) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> CollectionJoin<E, Y> join(CollectionAttribute<? super E, Y> collection, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> SetJoin<E, Y> join(SetAttribute<? super E, Y> set, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> ListJoin<E, Y> join(ListAttribute<? super E, Y> list, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <K, V> MapJoin<E, K, V> join(MapAttribute<? super E, K, V> map, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <X, Y> SetJoin<X, Y> joinSet(String attributeName) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <X, Y> ListJoin<X, Y> joinList(String attributeName) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <X, Y> jakarta.persistence.criteria.Join<X, Y> join(String attributeName, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <X, Y> SetJoin<X, Y> joinSet(String attributeName, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <X, Y> ListJoin<X, Y> joinList(String attributeName, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Set<Fetch<E, ?>> getFetches() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> Fetch<E, Y> fetch(SingularAttribute<? super E, Y> attribute) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> Fetch<E, Y> fetch(SingularAttribute<? super E, Y> attribute, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> Fetch<E, Y> fetch(PluralAttribute<? super E, ?, Y> attribute) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> Fetch<E, Y> fetch(PluralAttribute<? super E, ?, Y> attribute, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <X, Y> Fetch<X, Y> fetch(String attributeName) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <X, Y> Fetch<X, Y> fetch(String attributeName, JoinType jt) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> Path<Y> get(SingularAttribute<? super E, Y> attribute) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<E, K, V> map) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <K, C extends Collection<K>> Expression<C> get(PluralAttribute<E, C, K> collection) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Expression<Class<? extends E>> type() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Class<? extends E> getJavaType() {
      throw CriteriaUtils.notSupportedOperation();
   }
}
