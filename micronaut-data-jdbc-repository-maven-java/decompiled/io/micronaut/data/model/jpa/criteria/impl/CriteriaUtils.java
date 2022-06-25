package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.model.jpa.criteria.IExpression;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.predicate.ConjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.DisjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyBinaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyInValuesPredicate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.ParameterExpression;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Internal
public final class CriteriaUtils {
   private CriteriaUtils() {
   }

   public static boolean isNumeric(@NonNull Class<?> clazz) {
      return clazz.isPrimitive() ? Number.class.isAssignableFrom(ReflectionUtils.getPrimitiveType(clazz)) : Number.class.isAssignableFrom(clazz);
   }

   public static List<IExpression<Boolean>> requireBoolExpressions(Iterable<? extends Expression<?>> restrictions) {
      return (List<IExpression<Boolean>>)CollectionUtils.iterableToList(restrictions)
         .stream()
         .map(CriteriaUtils::requireBoolExpression)
         .collect(Collectors.toList());
   }

   public static IExpression<Boolean> requireBoolExpression(Expression<?> exp) {
      if (exp instanceof IExpression) {
         IExpression<Boolean> expression = (IExpression)exp;
         if (!expression.isBoolean()) {
            throw new IllegalStateException("Expected a boolean expression! Got: " + exp);
         } else {
            return expression;
         }
      } else {
         throw new IllegalStateException("Expression is unknown! Got: " + exp);
      }
   }

   public static <T> PersistentPropertyPath<T> requireBoolProperty(Expression<Boolean> exp) {
      if (exp instanceof PersistentPropertyPath) {
         PersistentPropertyPath<T> propertyPath = (PersistentPropertyPath)exp;
         if (!propertyPath.isBoolean()) {
            throw new IllegalStateException("Expected a boolean expression property! Got: " + exp);
         } else {
            return propertyPath;
         }
      } else {
         throw new IllegalStateException("Expression is expected to be a property path! Got: " + exp);
      }
   }

   public static <T> PersistentPropertyPath<T> requireNumericProperty(Expression<T> exp) {
      if (exp instanceof PersistentPropertyPath) {
         PersistentPropertyPath<T> propertyPath = (PersistentPropertyPath)exp;
         if (!propertyPath.isNumeric()) {
            throw new IllegalStateException("Expected a numeric expression property! Got: " + exp);
         } else {
            return propertyPath;
         }
      } else {
         throw new IllegalStateException("Expression is expected to be a property path! Got: " + exp);
      }
   }

   public static <T> Expression<T> requireNumericPropertyParameterOrLiteral(Expression<T> exp) {
      exp = requirePropertyParameterOrLiteral(exp);
      if (exp instanceof PersistentPropertyPath) {
         PersistentPropertyPath<?> propertyPath = (PersistentPropertyPath)exp;
         if (!propertyPath.isNumeric()) {
            throw new IllegalStateException("Expected a numeric expression property! Got: " + exp);
         } else {
            return exp;
         }
      } else if (exp instanceof ParameterExpression) {
         return exp;
      } else {
         return exp instanceof LiteralExpression ? exp : exp;
      }
   }

   public static <T> ParameterExpression<T> requireParameter(Expression<T> exp) {
      if (exp instanceof ParameterExpression) {
         return (ParameterExpression<T>)exp;
      } else {
         throw new IllegalStateException("Expression is expected to be a parameter! Got: " + exp);
      }
   }

   public static <T> PersistentPropertyPath<T> requireProperty(Expression<? extends T> exp) {
      if (exp instanceof PersistentPropertyPath) {
         return (PersistentPropertyPath<T>)exp;
      } else {
         throw new IllegalStateException("Expression is expected to be a property path! Got: " + exp);
      }
   }

   public static <T> Expression<T> requirePropertyParameterOrLiteral(Expression<T> exp) {
      if (!(exp instanceof PersistentPropertyPath) && !(exp instanceof ParameterExpression) && !(exp instanceof LiteralExpression)) {
         throw new IllegalStateException("Expression is expected to be a property path, a parameter or literal! Got: " + exp);
      } else {
         return exp;
      }
   }

   public static <T> IExpression<T> requirePropertyOrRoot(Expression<T> exp) {
      if (!(exp instanceof PersistentPropertyPath) && !(exp instanceof PersistentEntityRoot)) {
         throw new IllegalStateException("Expression is expected to be a property path or a root! Got: " + exp);
      } else {
         return (IExpression<T>)exp;
      }
   }

   public static IllegalStateException notSupportedOperation() {
      return new IllegalStateException("Not supported operation!");
   }

   public static boolean hasVersionPredicate(Expression<?> predicate) {
      if (predicate instanceof PersistentPropertyBinaryPredicate) {
         PersistentPropertyBinaryPredicate<?> pp = (PersistentPropertyBinaryPredicate)predicate;
         return pp.getProperty() == pp.getProperty().getOwner().getVersion();
      } else {
         if (predicate instanceof ConjunctionPredicate) {
            ConjunctionPredicate conjunctionPredicate = (ConjunctionPredicate)predicate;

            for(IExpression<Boolean> pred : conjunctionPredicate.getPredicates()) {
               if (hasVersionPredicate(pred)) {
                  return true;
               }
            }
         }

         if (predicate instanceof DisjunctionPredicate) {
            DisjunctionPredicate disjunctionPredicate = (DisjunctionPredicate)predicate;

            for(IExpression<Boolean> pred : disjunctionPredicate.getPredicates()) {
               if (hasVersionPredicate(pred)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public static Set<ParameterExpression<?>> extractPredicateParameters(Expression<?> predicate) {
      if (predicate == null) {
         return Collections.emptySet();
      } else {
         Set<ParameterExpression<?>> properties = new LinkedHashSet();
         extractPredicateParameters(predicate, properties);
         return properties;
      }
   }

   private static void extractPredicateParameters(Expression<?> predicate, Set<ParameterExpression<?>> parameters) {
      if (predicate instanceof PersistentPropertyBinaryPredicate) {
         PersistentPropertyBinaryPredicate<?> pp = (PersistentPropertyBinaryPredicate)predicate;
         if (pp.getExpression() instanceof ParameterExpression) {
            parameters.add((ParameterExpression)pp.getExpression());
         }
      } else if (predicate instanceof PersistentPropertyInValuesPredicate) {
         PersistentPropertyInValuesPredicate<?> pp = (PersistentPropertyInValuesPredicate)predicate;

         for(Expression<?> expression : pp.getValues()) {
            if (expression instanceof ParameterExpression) {
               parameters.add((ParameterExpression)expression);
            }
         }
      } else if (predicate instanceof ConjunctionPredicate) {
         ConjunctionPredicate conjunctionPredicate = (ConjunctionPredicate)predicate;

         for(IExpression<Boolean> pred : conjunctionPredicate.getPredicates()) {
            extractPredicateParameters(pred, parameters);
         }
      } else if (predicate instanceof DisjunctionPredicate) {
         DisjunctionPredicate disjunctionPredicate = (DisjunctionPredicate)predicate;

         for(IExpression<Boolean> pred : disjunctionPredicate.getPredicates()) {
            extractPredicateParameters(pred, parameters);
         }
      }

   }
}
