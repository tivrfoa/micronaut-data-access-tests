package io.micronaut.data.model;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.model.naming.NamingStrategy;
import java.util.Optional;

public interface Association extends PersistentProperty {
   default String getAliasName() {
      return NamingStrategy.DEFAULT.mappedName(this.getName()) + "_";
   }

   @NonNull
   PersistentEntity getAssociatedEntity();

   default Optional<? extends Association> getInverseSide() {
      return this.getAnnotationMetadata().stringValue(Relation.class, "mappedBy").flatMap(s -> {
         PersistentProperty persistentProperty = (PersistentProperty)this.getAssociatedEntity().getPropertyByPath(s).orElse(null);
         return persistentProperty instanceof Association ? Optional.of((Association)persistentProperty) : Optional.empty();
      });
   }

   default Optional<PersistentAssociationPath> getInversePathSide() {
      return this.getAnnotationMetadata().stringValue(Relation.class, "mappedBy").flatMap(s -> {
         PersistentPropertyPath persistentPropertyPath = this.getAssociatedEntity().getPropertyPath(s);
         return persistentPropertyPath instanceof PersistentAssociationPath ? Optional.of((PersistentAssociationPath)persistentPropertyPath) : Optional.empty();
      });
   }

   default boolean isBidirectional() {
      return this.getInverseSide().isPresent();
   }

   @NonNull
   default Relation.Kind getKind() {
      return (Relation.Kind)this.findAnnotation(Relation.class).flatMap(av -> av.enumValue(Relation.Kind.class)).orElse(Relation.Kind.ONE_TO_ONE);
   }

   default boolean isForeignKey() {
      Relation.Kind kind = this.getKind();
      return kind == Relation.Kind.ONE_TO_MANY
         || kind == Relation.Kind.MANY_TO_MANY
         || kind == Relation.Kind.ONE_TO_ONE && this.getAnnotationMetadata().stringValue(Relation.class, "mappedBy").isPresent();
   }

   default boolean doesCascade(Relation.Cascade... types) {
      if (ArrayUtils.isNotEmpty(types)) {
         String[] cascades = this.getAnnotationMetadata().stringValues(Relation.class, "cascade");

         for(String cascade : cascades) {
            if (cascade.equals("ALL")) {
               return true;
            }

            for(Relation.Cascade type : types) {
               String n = type.name();
               if (n.equals(cascade)) {
                  return true;
               }
            }
         }
      }

      return false;
   }
}
