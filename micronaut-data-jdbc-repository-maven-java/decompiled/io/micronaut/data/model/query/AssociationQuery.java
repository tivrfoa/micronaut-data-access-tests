package io.micronaut.data.model.query;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Association;

public class AssociationQuery extends DefaultQuery implements QueryModel.Criterion {
   private final Association association;
   private final String path;

   public AssociationQuery(String path, @NonNull Association association) {
      super(association.getAssociatedEntity());
      this.path = path;
      this.association = association;
   }

   public String getPath() {
      return this.path;
   }

   public Association getAssociation() {
      return this.association;
   }
}
