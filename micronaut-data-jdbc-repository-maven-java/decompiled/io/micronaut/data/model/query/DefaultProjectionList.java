package io.micronaut.data.model.query;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.query.factory.Projections;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DefaultProjectionList implements ProjectionList {
   private List<QueryModel.Projection> projections = new ArrayList(3);

   public List<QueryModel.Projection> getProjectionList() {
      return Collections.unmodifiableList(this.projections);
   }

   @Override
   public ProjectionList add(@NonNull QueryModel.Projection p) {
      if (p instanceof QueryModel.CountProjection) {
         if (this.projections.size() > 1) {
            throw new IllegalArgumentException("Cannot count on more than one projection");
         }

         if (this.projections.isEmpty()) {
            this.projections.add(p);
         } else {
            QueryModel.Projection existing = (QueryModel.Projection)this.projections.iterator().next();
            if (existing instanceof QueryModel.CountProjection) {
               return this;
            }

            if (existing instanceof QueryModel.PropertyProjection) {
               this.projections.clear();
               QueryModel.PropertyProjection pp = (QueryModel.PropertyProjection)existing;
               QueryModel.CountDistinctProjection newProjection = new QueryModel.CountDistinctProjection(pp.getPropertyName());
               this.projections.add(newProjection);
            } else if (existing instanceof QueryModel.IdProjection || existing instanceof QueryModel.DistinctProjection) {
               this.projections.clear();
               this.projections.add(new QueryModel.CountProjection());
            }
         }
      } else {
         this.projections.add(p);
      }

      return this;
   }

   @Override
   public ProjectionList id() {
      this.add(Projections.id());
      return this;
   }

   @Override
   public ProjectionList count() {
      this.add(Projections.count());
      return this;
   }

   @Override
   public ProjectionList countDistinct(String property) {
      this.add(Projections.countDistinct(property));
      return this;
   }

   @Override
   public ProjectionList groupProperty(String property) {
      this.add(Projections.groupProperty(property));
      return this;
   }

   public boolean isEmpty() {
      return this.projections.isEmpty();
   }

   @Override
   public ProjectionList distinct() {
      this.add(Projections.distinct());
      return this;
   }

   @Override
   public ProjectionList distinct(String property) {
      this.add(Projections.distinct(property));
      return this;
   }

   @Override
   public ProjectionList rowCount() {
      return this.count();
   }

   @Override
   public ProjectionList property(String name) {
      this.add(Projections.property(name));
      return this;
   }

   @Override
   public ProjectionList sum(String name) {
      this.add(Projections.sum(name));
      return this;
   }

   @Override
   public ProjectionList min(String name) {
      this.add(Projections.min(name));
      return this;
   }

   @Override
   public ProjectionList max(String name) {
      this.add(Projections.max(name));
      return this;
   }

   @Override
   public ProjectionList avg(String name) {
      this.add(Projections.avg(name));
      return this;
   }
}
