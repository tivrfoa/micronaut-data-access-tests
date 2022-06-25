package io.micronaut.data.model.query.factory;

import io.micronaut.data.model.query.QueryModel;

public class Projections {
   public static final QueryModel.IdProjection ID_PROJECTION = new QueryModel.IdProjection();
   public static final QueryModel.CountProjection COUNT_PROJECTION = new QueryModel.CountProjection();

   public static QueryModel.IdProjection id() {
      return ID_PROJECTION;
   }

   public static QueryModel.CountProjection count() {
      return COUNT_PROJECTION;
   }

   public static QueryModel.LiteralProjection literal(Object value) {
      return new QueryModel.LiteralProjection(value);
   }

   public static QueryModel.PropertyProjection property(String name) {
      return new QueryModel.PropertyProjection(name);
   }

   public static QueryModel.SumProjection sum(String name) {
      return new QueryModel.SumProjection(name);
   }

   public static QueryModel.MinProjection min(String name) {
      return new QueryModel.MinProjection(name);
   }

   public static QueryModel.MaxProjection max(String name) {
      return new QueryModel.MaxProjection(name);
   }

   public static QueryModel.AvgProjection avg(String name) {
      return new QueryModel.AvgProjection(name);
   }

   public static QueryModel.DistinctProjection distinct() {
      return new QueryModel.DistinctProjection();
   }

   public static QueryModel.DistinctPropertyProjection distinct(String property) {
      return new QueryModel.DistinctPropertyProjection(property);
   }

   public static QueryModel.CountDistinctProjection countDistinct(String property) {
      return new QueryModel.CountDistinctProjection(property);
   }

   public static QueryModel.GroupPropertyProjection groupProperty(String property) {
      return new QueryModel.GroupPropertyProjection(property);
   }
}
