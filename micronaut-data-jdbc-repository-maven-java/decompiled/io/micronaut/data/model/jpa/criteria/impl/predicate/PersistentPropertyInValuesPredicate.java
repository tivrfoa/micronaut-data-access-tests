package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.LiteralExpression;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitor;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Internal
public final class PersistentPropertyInValuesPredicate<T> extends AbstractPersistentPropertyPredicate<T> implements In<T> {
   private final List<Expression<?>> values;

   public PersistentPropertyInValuesPredicate(PersistentPropertyPath<T> propertyPath) {
      this(propertyPath, Collections.emptyList());
   }

   public PersistentPropertyInValuesPredicate(PersistentPropertyPath<T> propertyPath, Collection<Expression<?>> values) {
      super(propertyPath);
      this.values = new ArrayList(values);
   }

   public List<Expression<?>> getValues() {
      return this.values;
   }

   public Expression<T> getExpression() {
      return this.getPropertyPath();
   }

   public PersistentPropertyInValuesPredicate<T> value(T value) {
      this.values.add(new LiteralExpression(value));
      return this;
   }

   public PersistentPropertyInValuesPredicate<T> value(Expression<? extends T> value) {
      this.values.add(value);
      return this;
   }

   @Override
   public void accept(PredicateVisitor predicateVisitor) {
      predicateVisitor.visit(this);
   }

   public String toString() {
      return "PersistentPropertyInValuesPredicate{persistentPropertyPath=" + this.persistentPropertyPath + ", values=" + this.values + '}';
   }
}
