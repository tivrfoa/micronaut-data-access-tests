package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaBuilder;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaQuery;
import io.micronaut.data.model.jpa.criteria.impl.predicate.ConjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.DisjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.ExpressionBinaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.NegatedPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyBetweenPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyBinaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyInValuesPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyUnaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PredicateBinaryOp;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PredicateUnaryOp;
import io.micronaut.data.model.jpa.criteria.impl.selection.AggregateExpression;
import io.micronaut.data.model.jpa.criteria.impl.selection.AggregateType;
import io.micronaut.data.model.query.BindingParameter;
import io.micronaut.data.model.query.builder.QueryParameterBinding;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CollectionJoin;
import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.CriteriaBuilder.Case;
import jakarta.persistence.criteria.CriteriaBuilder.Coalesce;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.CriteriaBuilder.SimpleCase;
import jakarta.persistence.criteria.CriteriaBuilder.Trimspec;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

@Internal
public abstract class AbstractCriteriaBuilder implements PersistentEntityCriteriaBuilder {
   @NotNull
   private Predicate predicate(Expression<?> x, Expression<?> y, PredicateBinaryOp op) {
      return (Predicate)(x instanceof IdExpression
         ? new ExpressionBinaryPredicate(x, y, op)
         : new PersistentPropertyBinaryPredicate(CriteriaUtils.requireProperty(x), CriteriaUtils.requirePropertyParameterOrLiteral(y), op));
   }

   @NotNull
   private Predicate predicate(Expression<?> x, Object y, PredicateBinaryOp op) {
      return (Predicate)(x instanceof IdExpression
         ? new ExpressionBinaryPredicate(x, (Expression<?>)Objects.requireNonNull(this.<Object>literal(y)), op)
         : new PersistentPropertyBinaryPredicate(CriteriaUtils.requireProperty(x), (Expression<?>)Objects.requireNonNull(this.<Object>literal(y)), op));
   }

   @NonNull
   @Override
   public PersistentEntityCriteriaQuery<Tuple> createTupleQuery() {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <Y> CompoundSelection<Y> construct(@NonNull Class<Y> resultClass, @NonNull Selection<?>... selections) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public CompoundSelection<Tuple> tuple(@NonNull Selection<?>... selections) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public CompoundSelection<Object[]> array(@NonNull Selection<?>... selections) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Order asc(@NonNull Expression<?> x) {
      return new PersistentPropertyOrder(CriteriaUtils.requireProperty(x), true);
   }

   @NonNull
   public Order desc(@NonNull Expression<?> x) {
      return new PersistentPropertyOrder(CriteriaUtils.requireProperty(x), false);
   }

   @NonNull
   public <N extends Number> Expression<Double> avg(@NonNull Expression<N> x) {
      return new AggregateExpression(CriteriaUtils.requireNumericProperty(x), AggregateType.AVG);
   }

   @NonNull
   public <N extends Number> Expression<N> sum(@NonNull Expression<N> x) {
      return new AggregateExpression(CriteriaUtils.requireNumericProperty(x), AggregateType.SUM);
   }

   @NonNull
   public Expression<Long> sumAsLong(@NonNull Expression<Integer> x) {
      return new AggregateExpression(CriteriaUtils.requireNumericProperty(x), AggregateType.SUM, Long.class);
   }

   @NonNull
   public Expression<Double> sumAsDouble(@NonNull Expression<Float> x) {
      return new AggregateExpression(CriteriaUtils.requireNumericProperty(x), AggregateType.SUM, Double.class);
   }

   @NonNull
   public <N extends Number> Expression<N> max(@NonNull Expression<N> x) {
      return new AggregateExpression(CriteriaUtils.requireNumericProperty(x), AggregateType.MAX);
   }

   @NonNull
   public <N extends Number> Expression<N> min(@NonNull Expression<N> x) {
      return new AggregateExpression(CriteriaUtils.requireNumericProperty(x), AggregateType.MIN);
   }

   @NonNull
   public <X extends Comparable<? super X>> Expression<X> greatest(@NonNull Expression<X> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <X extends Comparable<? super X>> Expression<X> least(@NonNull Expression<X> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Long> count(@NonNull Expression<?> x) {
      return new AggregateExpression(CriteriaUtils.requirePropertyOrRoot(x), AggregateType.COUNT, Long.class);
   }

   @NonNull
   public Expression<Long> countDistinct(@NonNull Expression<?> x) {
      return new AggregateExpression(CriteriaUtils.requirePropertyOrRoot(x), AggregateType.COUNT_DISTINCT, Long.class);
   }

   @NonNull
   public Predicate exists(@NonNull Subquery<?> subquery) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <Y> Expression<Y> all(@NonNull Subquery<Y> subquery) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <Y> Expression<Y> some(@NonNull Subquery<Y> subquery) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <Y> Expression<Y> any(@NonNull Subquery<Y> subquery) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Predicate and(@NonNull Expression<Boolean> x, @NonNull Expression<Boolean> y) {
      return new ConjunctionPredicate(Arrays.asList(CriteriaUtils.requireBoolExpression(x), CriteriaUtils.requireBoolExpression(y)));
   }

   @NonNull
   public Predicate and(@NonNull Predicate... restrictions) {
      return this.and(Arrays.asList(restrictions));
   }

   @NonNull
   @Override
   public Predicate and(@NonNull Iterable<Predicate> restrictions) {
      return new ConjunctionPredicate(CriteriaUtils.requireBoolExpressions(restrictions));
   }

   @NonNull
   @Override
   public Predicate isEmptyString(@NonNull Expression<String> expression) {
      return new PersistentPropertyUnaryPredicate(CriteriaUtils.requireProperty(expression), PredicateUnaryOp.IS_EMPTY);
   }

   @NonNull
   @Override
   public Predicate isNotEmptyString(@NonNull Expression<String> expression) {
      return new PersistentPropertyUnaryPredicate(CriteriaUtils.requireProperty(expression), PredicateUnaryOp.IS_NOT_EMPTY);
   }

   @NonNull
   @Override
   public Predicate rlikeString(@NonNull Expression<String> x, @NonNull Expression<String> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireProperty(x), CriteriaUtils.requirePropertyParameterOrLiteral(y), PredicateBinaryOp.RLIKE
      );
   }

   @NonNull
   @Override
   public Predicate ilikeString(@NonNull Expression<String> x, @NonNull Expression<String> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireProperty(x), CriteriaUtils.requirePropertyParameterOrLiteral(y), PredicateBinaryOp.ILIKE
      );
   }

   @Override
   public Predicate endingWithString(@NonNull Expression<String> x, @NonNull Expression<String> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireProperty(x), CriteriaUtils.requirePropertyParameterOrLiteral(y), PredicateBinaryOp.ENDS_WITH
      );
   }

   @NonNull
   @Override
   public Predicate startsWithString(@NonNull Expression<String> x, @NonNull Expression<String> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireProperty(x), CriteriaUtils.requirePropertyParameterOrLiteral(y), PredicateBinaryOp.STARTS_WITH
      );
   }

   @NonNull
   @Override
   public Predicate containsString(@NonNull Expression<String> x, @NonNull Expression<String> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireProperty(x), CriteriaUtils.requirePropertyParameterOrLiteral(y), PredicateBinaryOp.CONTAINS
      );
   }

   @NonNull
   @Override
   public Predicate equalStringIgnoreCase(@NonNull Expression<String> x, @NonNull String y) {
      return new PersistentPropertyBinaryPredicate(CriteriaUtils.requireProperty(x), this.literal(y), PredicateBinaryOp.EQUALS_IGNORE_CASE);
   }

   @NonNull
   @Override
   public Predicate equalStringIgnoreCase(@NonNull Expression<String> x, @NonNull Expression<String> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireProperty(x), CriteriaUtils.requirePropertyParameterOrLiteral(y), PredicateBinaryOp.EQUALS_IGNORE_CASE
      );
   }

   @NonNull
   @Override
   public Predicate notEqualStringIgnoreCase(@NonNull Expression<String> x, @NonNull String y) {
      return new PersistentPropertyBinaryPredicate(CriteriaUtils.requireProperty(x), this.literal(y), PredicateBinaryOp.NOT_EQUALS_IGNORE_CASE);
   }

   @NonNull
   @Override
   public Predicate notEqualStringIgnoreCase(@NonNull Expression<String> x, @NonNull Expression<String> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireProperty(x), CriteriaUtils.requirePropertyParameterOrLiteral(y), PredicateBinaryOp.NOT_EQUALS_IGNORE_CASE
      );
   }

   @NonNull
   @Override
   public Predicate startsWithStringIgnoreCase(@NonNull Expression<String> x, @NonNull Expression<String> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireProperty(x), CriteriaUtils.requirePropertyParameterOrLiteral(y), PredicateBinaryOp.STARTS_WITH_IGNORE_CASE
      );
   }

   @NonNull
   @Override
   public Predicate endingWithStringIgnoreCase(@NonNull Expression<String> x, @NonNull Expression<String> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireProperty(x), CriteriaUtils.requirePropertyParameterOrLiteral(y), PredicateBinaryOp.ENDS_WITH_IGNORE_CASE
      );
   }

   @NonNull
   public Predicate or(@NonNull Expression<Boolean> x, @NonNull Expression<Boolean> y) {
      return new DisjunctionPredicate(Arrays.asList(CriteriaUtils.requireBoolExpression(x), CriteriaUtils.requireBoolExpression(y)));
   }

   @NonNull
   public Predicate or(@NonNull Predicate... restrictions) {
      return this.or(Arrays.asList(restrictions));
   }

   @NonNull
   @Override
   public Predicate or(@NonNull Iterable<Predicate> restrictions) {
      return new DisjunctionPredicate(CriteriaUtils.requireBoolExpressions(restrictions));
   }

   @NonNull
   public Predicate not(@NonNull Expression<Boolean> restriction) {
      return new NegatedPredicate(CriteriaUtils.requireBoolExpression(restriction));
   }

   @NonNull
   public Predicate conjunction() {
      return new ConjunctionPredicate(Collections.emptyList());
   }

   @NonNull
   public Predicate disjunction() {
      return new DisjunctionPredicate(Collections.emptyList());
   }

   @NonNull
   public Predicate isTrue(@NonNull Expression<Boolean> x) {
      return new PersistentPropertyUnaryPredicate(CriteriaUtils.requireBoolProperty(x), PredicateUnaryOp.IS_TRUE);
   }

   @NonNull
   public Predicate isFalse(@NonNull Expression<Boolean> x) {
      return new PersistentPropertyUnaryPredicate(CriteriaUtils.requireProperty(x), PredicateUnaryOp.IS_FALSE);
   }

   @NonNull
   public Predicate isNull(@NonNull Expression<?> x) {
      return new PersistentPropertyUnaryPredicate(CriteriaUtils.requireProperty(x), PredicateUnaryOp.IS_NULL);
   }

   @NonNull
   public Predicate isNotNull(@NonNull Expression<?> x) {
      return new PersistentPropertyUnaryPredicate(CriteriaUtils.requireProperty(x), PredicateUnaryOp.IS_NON_NULL);
   }

   @NonNull
   public Predicate equal(@NonNull Expression<?> x, @NonNull Expression<?> y) {
      return this.predicate(x, y, PredicateBinaryOp.EQUALS);
   }

   @NonNull
   public Predicate equal(@NonNull Expression<?> x, @Nullable Object y) {
      return this.predicate(x, y, PredicateBinaryOp.EQUALS);
   }

   @NonNull
   public Predicate notEqual(@NonNull Expression<?> x, @NonNull Expression<?> y) {
      return this.predicate(x, y, PredicateBinaryOp.NOT_EQUALS);
   }

   @NonNull
   public Predicate notEqual(@NonNull Expression<?> x, @Nullable Object y) {
      return this.predicate(x, y, PredicateBinaryOp.NOT_EQUALS);
   }

   @NonNull
   public <Y extends Comparable<? super Y>> Predicate greaterThan(@NonNull Expression<? extends Y> x, @NonNull Expression<? extends Y> y) {
      return this.predicate(x, y, PredicateBinaryOp.GREATER_THAN);
   }

   @NonNull
   public <Y extends Comparable<? super Y>> Predicate greaterThan(@NonNull Expression<? extends Y> x, Y y) {
      return this.predicate(x, y, PredicateBinaryOp.GREATER_THAN);
   }

   @NonNull
   public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(@NonNull Expression<? extends Y> x, @NonNull Expression<? extends Y> y) {
      return this.predicate(x, y, PredicateBinaryOp.GREATER_THAN_OR_EQUALS);
   }

   @NonNull
   public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(@NonNull Expression<? extends Y> x, Y y) {
      return this.predicate(x, y, PredicateBinaryOp.GREATER_THAN_OR_EQUALS);
   }

   @NonNull
   public <Y extends Comparable<? super Y>> Predicate lessThan(@NonNull Expression<? extends Y> x, @NonNull Expression<? extends Y> y) {
      return this.predicate(x, y, PredicateBinaryOp.LESS_THAN);
   }

   @NonNull
   public <Y extends Comparable<? super Y>> Predicate lessThan(@NonNull Expression<? extends Y> x, @NonNull Y y) {
      return this.predicate(x, y, PredicateBinaryOp.LESS_THAN);
   }

   @NonNull
   public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(@NonNull Expression<? extends Y> x, @NonNull Expression<? extends Y> y) {
      return this.predicate(x, y, PredicateBinaryOp.LESS_THAN_OR_EQUALS);
   }

   @NonNull
   public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(@NonNull Expression<? extends Y> x, Y y) {
      return this.predicate(x, y, PredicateBinaryOp.LESS_THAN_OR_EQUALS);
   }

   @NonNull
   public <Y extends Comparable<? super Y>> Predicate between(
      @NonNull Expression<? extends Y> v, @NonNull Expression<? extends Y> x, @NonNull Expression<? extends Y> y
   ) {
      return new PersistentPropertyBetweenPredicate(
         CriteriaUtils.requireProperty(v), CriteriaUtils.requireNumericPropertyParameterOrLiteral(x), CriteriaUtils.requireNumericPropertyParameterOrLiteral(y)
      );
   }

   @NonNull
   public <Y extends Comparable<? super Y>> Predicate between(@NonNull Expression<? extends Y> v, @NonNull Y x, @NonNull Y y) {
      return new PersistentPropertyBetweenPredicate(CriteriaUtils.requireProperty(v), Objects.requireNonNull(x), Objects.requireNonNull(y));
   }

   @NonNull
   public Predicate gt(@NonNull Expression<? extends Number> x, @NonNull Expression<? extends Number> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireNumericProperty(x), CriteriaUtils.requireNumericPropertyParameterOrLiteral(y), PredicateBinaryOp.GREATER_THAN
      );
   }

   @NonNull
   public Predicate gt(@NonNull Expression<? extends Number> x, @NonNull Number y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireNumericProperty(x), (Expression<?>)Objects.requireNonNull(this.literal(y)), PredicateBinaryOp.GREATER_THAN
      );
   }

   @NonNull
   public Predicate ge(@NonNull Expression<? extends Number> x, @NonNull Expression<? extends Number> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireNumericProperty(x), CriteriaUtils.requireNumericPropertyParameterOrLiteral(y), PredicateBinaryOp.GREATER_THAN_OR_EQUALS
      );
   }

   @NonNull
   public Predicate ge(@NonNull Expression<? extends Number> x, @NonNull Number y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireNumericProperty(x), (Expression<?>)Objects.requireNonNull(this.literal(y)), PredicateBinaryOp.GREATER_THAN_OR_EQUALS
      );
   }

   @NonNull
   public Predicate lt(@NonNull Expression<? extends Number> x, @NonNull Expression<? extends Number> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireNumericProperty(x), CriteriaUtils.requireNumericPropertyParameterOrLiteral(y), PredicateBinaryOp.LESS_THAN
      );
   }

   @NonNull
   public Predicate lt(@NonNull Expression<? extends Number> x, @NonNull Number y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireNumericProperty(x), (Expression<?>)Objects.requireNonNull(this.literal(y)), PredicateBinaryOp.LESS_THAN
      );
   }

   @NonNull
   public Predicate le(@NonNull Expression<? extends Number> x, @NonNull Expression<? extends Number> y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireNumericProperty(x), CriteriaUtils.requireNumericPropertyParameterOrLiteral(y), PredicateBinaryOp.LESS_THAN_OR_EQUALS
      );
   }

   @NonNull
   public Predicate le(@NonNull Expression<? extends Number> x, @NonNull Number y) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireNumericProperty(x), (Expression<?>)Objects.requireNonNull(this.literal(y)), PredicateBinaryOp.LESS_THAN_OR_EQUALS
      );
   }

   @NonNull
   public <N extends Number> Expression<N> neg(@NonNull Expression<N> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <N extends Number> Expression<N> abs(@NonNull Expression<N> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <N extends Number> Expression<N> sum(@NonNull Expression<? extends N> x, Expression<? extends N> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <N extends Number> Expression<N> sum(@NonNull Expression<? extends N> x, @NonNull N y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <N extends Number> Expression<N> sum(@NonNull N x, @NonNull Expression<? extends N> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <N extends Number> Expression<N> prod(@NonNull Expression<? extends N> x, @NonNull Expression<? extends N> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <N extends Number> Expression<N> prod(@NonNull Expression<? extends N> x, @NonNull N y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <N extends Number> Expression<N> prod(@NonNull N x, @NonNull Expression<? extends N> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <N extends Number> Expression<N> diff(@NonNull Expression<? extends N> x, @NonNull Expression<? extends N> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <N extends Number> Expression<N> diff(@NonNull Expression<? extends N> x, @NonNull N y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <N extends Number> Expression<N> diff(@NonNull N x, @NonNull Expression<? extends N> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Number> quot(@NonNull Expression<? extends Number> x, @NonNull Expression<? extends Number> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Number> quot(@NonNull Expression<? extends Number> x, @NonNull Number y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Number> quot(@NonNull Number x, @NonNull Expression<? extends Number> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Integer> mod(@NonNull Expression<Integer> x, @NonNull Expression<Integer> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Expression<Integer> mod(@NonNull Expression<Integer> x, @NonNull Integer y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Integer> mod(@NonNull Integer x, @NonNull Expression<Integer> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Double> sqrt(@NonNull Expression<? extends Number> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Long> toLong(@NonNull Expression<? extends Number> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Integer> toInteger(@NonNull Expression<? extends Number> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Float> toFloat(@NonNull Expression<? extends Number> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Double> toDouble(@NonNull Expression<? extends Number> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<BigDecimal> toBigDecimal(@NonNull Expression<? extends Number> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<BigInteger> toBigInteger(@NonNull Expression<? extends Number> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> toString(@NonNull Expression<Character> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <T> Expression<T> literal(@NonNull T value) {
      return new LiteralExpression<>(value);
   }

   @NonNull
   public <T> Expression<T> nullLiteral(@NonNull Class<T> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <T> ParameterExpression<T> parameter(@NonNull Class<T> paramClass) {
      return new ParameterExpressionImpl<T>(paramClass, null) {
         @Override
         public QueryParameterBinding bind(BindingParameter.BindingContext bindingContext) {
            final String name = bindingContext.getName() == null ? String.valueOf(bindingContext.getIndex()) : bindingContext.getName();
            final PersistentPropertyPath outgoingQueryParameterProperty = bindingContext.getOutgoingQueryParameterProperty();
            return new QueryParameterBinding() {
               @Override
               public String getKey() {
                  return name;
               }

               @Override
               public DataType getDataType() {
                  return outgoingQueryParameterProperty.getProperty().getDataType();
               }

               @Override
               public String[] getPropertyPath() {
                  return AbstractCriteriaBuilder.this.asStringPath(
                     outgoingQueryParameterProperty.getAssociations(), outgoingQueryParameterProperty.getProperty()
                  );
               }

               @Override
               public boolean isExpandable() {
                  return bindingContext.isExpandable();
               }
            };
         }
      };
   }

   private String[] asStringPath(List<Association> associations, PersistentProperty property) {
      if (associations.isEmpty()) {
         return new String[]{property.getName()};
      } else {
         List<String> path = new ArrayList(associations.size() + 1);

         for(Association association : associations) {
            path.add(association.getName());
         }

         path.add(property.getName());
         return (String[])path.toArray(new String[0]);
      }
   }

   @NonNull
   public <T> ParameterExpression<T> parameter(@NonNull Class<T> paramClass, @NonNull String name) {
      return new ParameterExpressionImpl<T>(paramClass, name) {
         @Override
         public QueryParameterBinding bind(BindingParameter.BindingContext bindingContext) {
            final String name = bindingContext.getName() == null ? String.valueOf(bindingContext.getIndex()) : bindingContext.getName();
            final PersistentPropertyPath outgoingQueryParameterProperty = bindingContext.getOutgoingQueryParameterProperty();
            return new QueryParameterBinding() {
               @Override
               public String getKey() {
                  return name;
               }

               @Override
               public DataType getDataType() {
                  return outgoingQueryParameterProperty.getProperty().getDataType();
               }

               @Override
               public String[] getPropertyPath() {
                  return AbstractCriteriaBuilder.this.asStringPath(
                     outgoingQueryParameterProperty.getAssociations(), outgoingQueryParameterProperty.getProperty()
                  );
               }
            };
         }
      };
   }

   @NonNull
   public <C extends Collection<?>> Predicate isEmpty(@NonNull Expression<C> collection) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <C extends Collection<?>> Predicate isNotEmpty(@NonNull Expression<C> collection) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <C extends Collection<?>> Expression<Integer> size(@NonNull Expression<C> collection) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <C extends Collection<?>> Expression<Integer> size(@NonNull C collection) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <E, C extends Collection<E>> Predicate isMember(@NonNull Expression<E> elem, @NonNull Expression<C> collection) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <E, C extends Collection<E>> Predicate isMember(@NonNull E elem, @NonNull Expression<C> collection) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <E, C extends Collection<E>> Predicate isNotMember(@NonNull Expression<E> elem, @NonNull Expression<C> collection) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <E, C extends Collection<E>> Predicate isNotMember(@NonNull E elem, @NonNull Expression<C> collection) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <V, M extends Map<?, V>> Expression<Collection<V>> values(@NonNull M map) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <K, M extends Map<K, ?>> Expression<Set<K>> keys(@NonNull M map) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Predicate like(@NonNull Expression<String> x, @NonNull Expression<String> pattern) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireProperty(x), CriteriaUtils.requirePropertyParameterOrLiteral(pattern), PredicateBinaryOp.LIKE
      );
   }

   @NonNull
   @Override
   public Predicate regex(@NonNull Expression<String> x, @NonNull Expression<String> pattern) {
      return new PersistentPropertyBinaryPredicate(
         CriteriaUtils.requireProperty(x), CriteriaUtils.requirePropertyParameterOrLiteral(pattern), PredicateBinaryOp.REGEX
      );
   }

   @NonNull
   public Predicate like(@NonNull Expression<String> x, @NonNull String pattern) {
      return new PersistentPropertyBinaryPredicate(CriteriaUtils.requireProperty(x), this.literal(pattern), PredicateBinaryOp.LIKE);
   }

   @NonNull
   public Predicate like(@NonNull Expression<String> x, @NonNull Expression<String> pattern, @NonNull Expression<Character> escapeChar) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Predicate like(@NonNull Expression<String> x, @NonNull Expression<String> pattern, char escapeChar) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Predicate like(@NonNull Expression<String> x, @NonNull String pattern, @NonNull Expression<Character> escapeChar) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Predicate like(@NonNull Expression<String> x, @NonNull String pattern, char escapeChar) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Predicate notLike(@NonNull Expression<String> x, @NonNull Expression<String> pattern) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Predicate notLike(@NonNull Expression<String> x, @NonNull String pattern) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Predicate notLike(@NonNull Expression<String> x, @NonNull Expression<String> pattern, @NonNull Expression<Character> escapeChar) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Predicate notLike(@NonNull Expression<String> x, @NonNull Expression<String> pattern, char escapeChar) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Predicate notLike(@NonNull Expression<String> x, @NonNull String pattern, @NonNull Expression<Character> escapeChar) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Predicate notLike(@NonNull Expression<String> x, @NonNull String pattern, char escapeChar) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> concat(@NonNull Expression<String> x, @NonNull Expression<String> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> concat(@NonNull Expression<String> x, @NonNull String y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> concat(@NonNull String x, @NonNull Expression<String> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> substring(@NonNull Expression<String> x, @NonNull Expression<Integer> from) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> substring(@NonNull Expression<String> x, int from) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> substring(@NonNull Expression<String> x, @NonNull Expression<Integer> from, @NonNull Expression<Integer> len) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> substring(@NonNull Expression<String> x, int from, int len) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> trim(@NonNull Expression<String> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> trim(@NonNull Trimspec ts, @NonNull Expression<String> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> trim(@NonNull Expression<Character> t, @NonNull Expression<String> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> trim(@NonNull Trimspec ts, @NonNull Expression<Character> t, @NonNull Expression<String> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> trim(char t, @NonNull Expression<String> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> trim(@NonNull Trimspec ts, char t, @NonNull Expression<String> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> lower(@NonNull Expression<String> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<String> upper(@NonNull Expression<String> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Integer> length(@NonNull Expression<String> x) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Integer> locate(@NonNull Expression<String> x, @NonNull Expression<String> pattern) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Integer> locate(@NonNull Expression<String> x, @NonNull String pattern) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Integer> locate(@NonNull Expression<String> x, @NonNull Expression<String> pattern, @NonNull Expression<Integer> from) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Integer> locate(@NonNull Expression<String> x, @NonNull String pattern, int from) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Date> currentDate() {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Timestamp> currentTimestamp() {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public Expression<Time> currentTime() {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <T> In<T> in(Expression<? extends T> expression) {
      return new PersistentPropertyInValuesPredicate<>(CriteriaUtils.requireProperty(expression));
   }

   @NonNull
   public <Y> Expression<Y> coalesce(@NonNull Expression<? extends Y> x, @NonNull Expression<? extends Y> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <Y> Expression<Y> coalesce(@NonNull Expression<? extends Y> x, Y y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <Y> Expression<Y> nullif(@NonNull Expression<Y> x, @NonNull Expression<?> y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <Y> Expression<Y> nullif(@NonNull Expression<Y> x, Y y) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <T> Coalesce<T> coalesce() {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <C, R> SimpleCase<C, R> selectCase(@NonNull Expression<? extends C> expression) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <R> Case<R> selectCase() {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <T> Expression<T> function(@NonNull String name, @NonNull Class<T> type, @NonNull Expression<?>... args) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <X, T, V extends T> Join<X, V> treat(@NonNull Join<X, T> join, @NonNull Class<V> type) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <X, T, E extends T> CollectionJoin<X, E> treat(@NonNull CollectionJoin<X, T> join, @NonNull Class<E> type) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <X, T, E extends T> SetJoin<X, E> treat(@NonNull SetJoin<X, T> join, @NonNull Class<E> type) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <X, T, E extends T> ListJoin<X, E> treat(@NonNull ListJoin<X, T> join, @NonNull Class<E> type) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <X, K, T, V extends T> MapJoin<X, K, V> treat(@NonNull MapJoin<X, K, T> join, @NonNull Class<V> type) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <X, T extends X> Path<T> treat(@NonNull Path<X> path, @NonNull Class<T> type) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   public <X, T extends X> Root<T> treat(@NonNull Root<X> root, @NonNull Class<T> type) {
      throw CriteriaUtils.notSupportedOperation();
   }
}
