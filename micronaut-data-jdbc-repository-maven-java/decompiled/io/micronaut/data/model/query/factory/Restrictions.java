package io.micronaut.data.model.query.factory;

import io.micronaut.data.model.query.QueryModel;

public class Restrictions {
   public static QueryModel.Equals eq(String property, Object parameter) {
      return new QueryModel.Equals(property, parameter);
   }

   public static QueryModel.IdEquals idEq(Object parameter) {
      return new QueryModel.IdEquals(parameter);
   }

   public static QueryModel.VersionEquals versionEq(Object parameter) {
      return new QueryModel.VersionEquals(parameter);
   }

   public static QueryModel.NotEquals ne(String property, Object parameter) {
      return new QueryModel.NotEquals(property, parameter);
   }

   public static QueryModel.In in(String property, Object parameter) {
      return new QueryModel.In(property, parameter);
   }

   public static QueryModel.NotIn notIn(String property, Object parameter) {
      return new QueryModel.NotIn(property, parameter);
   }

   public static QueryModel.In in(String property, QueryModel subquery) {
      return new QueryModel.In(property, subquery);
   }

   public static QueryModel.NotIn notIn(String property, QueryModel subquery) {
      return new QueryModel.NotIn(property, subquery);
   }

   public static QueryModel.Like like(String property, Object expression) {
      return new QueryModel.Like(property, expression);
   }

   public static QueryModel.Regex regex(String property, Object expression) {
      return new QueryModel.Regex(property, expression);
   }

   public static QueryModel.StartsWith startsWith(String property, Object expression) {
      return new QueryModel.StartsWith(property, expression);
   }

   public static QueryModel.Contains contains(String property, Object expression) {
      return new QueryModel.Contains(property, expression);
   }

   public static QueryModel.EndsWith endsWith(String property, Object expression) {
      return new QueryModel.EndsWith(property, expression);
   }

   public static QueryModel.ILike ilike(String property, Object expression) {
      return new QueryModel.ILike(property, expression);
   }

   public static QueryModel.RLike rlike(String property, Object expression) {
      return new QueryModel.RLike(property, expression);
   }

   public static QueryModel.Criterion and(QueryModel.Criterion a, QueryModel.Criterion b) {
      return new QueryModel.Conjunction().add(a).add(b);
   }

   public static QueryModel.Criterion or(QueryModel.Criterion a, QueryModel.Criterion b) {
      return new QueryModel.Disjunction().add(a).add(b);
   }

   public static QueryModel.Between between(String property, Object start, Object end) {
      return new QueryModel.Between(property, start, end);
   }

   public static QueryModel.GreaterThan gt(String property, Object parameter) {
      return new QueryModel.GreaterThan(property, parameter);
   }

   public static QueryModel.LessThan lt(String property, Object parameter) {
      return new QueryModel.LessThan(property, parameter);
   }

   public static QueryModel.GreaterThanEquals gte(String property, Object parameter) {
      return new QueryModel.GreaterThanEquals(property, parameter);
   }

   public static QueryModel.LessThanEquals lte(String property, Object parameter) {
      return new QueryModel.LessThanEquals(property, parameter);
   }

   public static QueryModel.IsNull isNull(String property) {
      return new QueryModel.IsNull(property);
   }

   public static QueryModel.IsEmpty isEmpty(String property) {
      return new QueryModel.IsEmpty(property);
   }

   public static QueryModel.IsNotEmpty isNotEmpty(String property) {
      return new QueryModel.IsNotEmpty(property);
   }

   public static QueryModel.IsNotNull isNotNull(String property) {
      return new QueryModel.IsNotNull(property);
   }

   public static QueryModel.IsTrue isTrue(String property) {
      return new QueryModel.IsTrue(property);
   }

   public static QueryModel.IsFalse isFalse(String property) {
      return new QueryModel.IsFalse(property);
   }

   public static QueryModel.SizeEquals sizeEq(String property, Object size) {
      return new QueryModel.SizeEquals(property, size);
   }

   public static QueryModel.SizeGreaterThan sizeGt(String property, Object size) {
      return new QueryModel.SizeGreaterThan(property, size);
   }

   public static QueryModel.SizeGreaterThanEquals sizeGe(String property, Object size) {
      return new QueryModel.SizeGreaterThanEquals(property, size);
   }

   public static QueryModel.SizeLessThanEquals sizeLe(String property, Object size) {
      return new QueryModel.SizeLessThanEquals(property, size);
   }

   public static QueryModel.SizeLessThan sizeLt(String property, Object size) {
      return new QueryModel.SizeLessThan(property, size);
   }

   public static QueryModel.SizeNotEquals sizeNe(String property, Object size) {
      return new QueryModel.SizeNotEquals(property, size);
   }

   public static QueryModel.EqualsProperty eqProperty(String propertyName, String otherPropertyName) {
      return new QueryModel.EqualsProperty(propertyName, otherPropertyName);
   }

   public static QueryModel.NotEqualsProperty neProperty(String propertyName, String otherPropertyName) {
      return new QueryModel.NotEqualsProperty(propertyName, otherPropertyName);
   }

   public static QueryModel.GreaterThanProperty gtProperty(String propertyName, String otherPropertyName) {
      return new QueryModel.GreaterThanProperty(propertyName, otherPropertyName);
   }

   public static QueryModel.GreaterThanEqualsProperty geProperty(String propertyName, String otherPropertyName) {
      return new QueryModel.GreaterThanEqualsProperty(propertyName, otherPropertyName);
   }

   public static QueryModel.LessThanProperty ltProperty(String propertyName, String otherPropertyName) {
      return new QueryModel.LessThanProperty(propertyName, otherPropertyName);
   }

   public static QueryModel.LessThanEqualsProperty leProperty(String propertyName, String otherPropertyName) {
      return new QueryModel.LessThanEqualsProperty(propertyName, otherPropertyName);
   }
}
