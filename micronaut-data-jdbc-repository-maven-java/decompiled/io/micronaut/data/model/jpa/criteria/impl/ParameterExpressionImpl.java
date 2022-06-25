package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.query.BindingParameter;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Selection;
import java.util.Collection;
import java.util.List;

@Internal
public abstract class ParameterExpressionImpl<T> implements ParameterExpression<T>, BindingParameter {
   private final Class<T> type;
   private final String name;

   public ParameterExpressionImpl(Class<T> type, String name) {
      this.type = type;
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public Integer getPosition() {
      return null;
   }

   public Class<T> getParameterType() {
      return this.type;
   }

   public Predicate isNull() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Predicate isNotNull() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Predicate in(Object... values) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Predicate in(Expression<?>... values) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Predicate in(Collection<?> values) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Predicate in(Expression<Collection<?>> values) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <X> Expression<X> as(Class<X> type) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Selection<T> alias(String name) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public boolean isCompoundSelection() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public List<Selection<?>> getCompoundSelectionItems() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Class<? extends T> getJavaType() {
      return this.getParameterType();
   }

   public String getAlias() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public String toString() {
      return "ParameterExpressionImpl{type=" + this.type + ", name='" + this.name + '\'' + '}';
   }
}
