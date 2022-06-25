package io.micronaut.data.model;

import java.util.List;

public final class PersistentAssociationPath extends PersistentPropertyPath {
   public PersistentAssociationPath(List<Association> associations, Association association) {
      super(associations, association);
   }

   public PersistentAssociationPath(List<Association> associations, Association association, String path) {
      super(associations, association, path);
   }

   public Association getProperty() {
      return (Association)super.getProperty();
   }

   public Association getAssociation() {
      return (Association)super.getProperty();
   }
}
