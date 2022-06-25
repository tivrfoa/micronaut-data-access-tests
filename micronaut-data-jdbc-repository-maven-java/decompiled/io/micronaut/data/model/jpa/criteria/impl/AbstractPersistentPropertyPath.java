package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.Bindable;
import jakarta.persistence.metamodel.MapAttribute;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Internal
public abstract class AbstractPersistentPropertyPath<T> implements PersistentPropertyPath<T>, SelectionVisitable {
   private final PersistentProperty persistentProperty;
   private final List<Association> path;

   public AbstractPersistentPropertyPath(PersistentProperty persistentProperty, List<Association> path) {
      this.persistentProperty = persistentProperty;
      this.path = path;
   }

   @Override
   public void accept(SelectionVisitor selectionVisitor) {
      selectionVisitor.visit(this);
   }

   @Override
   public PersistentProperty getProperty() {
      return this.persistentProperty;
   }

   @Override
   public List<Association> getAssociations() {
      return this.path;
   }

   public Bindable<T> getModel() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Path<?> getParentPath() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<T, C, E> collection) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<T, K, V> map) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> Path<Y> get(SingularAttribute<? super T, Y> attribute) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Expression<Class<? extends T>> type() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <Y> Path<Y> get(String attributeName) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Class<? extends T> getJavaType() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public String toString() {
      return "PersistentPropertyPath{persistentProperty=" + this.persistentProperty + ", path=" + this.path + '}';
   }
}
