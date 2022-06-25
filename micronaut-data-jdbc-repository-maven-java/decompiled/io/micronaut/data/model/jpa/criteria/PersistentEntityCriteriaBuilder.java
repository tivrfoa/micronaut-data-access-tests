package io.micronaut.data.model.jpa.criteria;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public interface PersistentEntityCriteriaBuilder extends CriteriaBuilder {
   PersistentEntityCriteriaQuery<Object> createQuery();

   <T> PersistentEntityCriteriaQuery<T> createQuery(Class<T> resultClass);

   PersistentEntityCriteriaQuery<Tuple> createTupleQuery();

   <T> PersistentEntityCriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity);

   <T> PersistentEntityCriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity);

   Predicate or(Iterable<Predicate> restrictions);

   Predicate and(Iterable<Predicate> restrictions);

   Predicate isEmptyString(Expression<String> expression);

   Predicate isNotEmptyString(Expression<String> expression);

   Predicate rlikeString(Expression<String> x, Expression<String> y);

   Predicate ilikeString(Expression<String> x, Expression<String> y);

   Predicate startsWithString(Expression<String> x, Expression<String> y);

   Predicate endingWithString(Expression<String> x, Expression<String> y);

   Predicate containsString(Expression<String> x, Expression<String> y);

   Predicate equalStringIgnoreCase(Expression<String> x, String y);

   Predicate equalStringIgnoreCase(Expression<String> x, Expression<String> y);

   Predicate notEqualStringIgnoreCase(Expression<String> x, String y);

   Predicate notEqualStringIgnoreCase(Expression<String> x, Expression<String> y);

   Predicate startsWithStringIgnoreCase(Expression<String> x, Expression<String> y);

   Predicate endingWithStringIgnoreCase(Expression<String> x, Expression<String> y);

   Predicate regex(Expression<String> x, Expression<String> pattern);
}
