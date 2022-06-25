package io.micronaut.data.model.naming;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.Embedded;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import java.util.List;

@FunctionalInterface
@Introspected
public interface NamingStrategy {
   NamingStrategy DEFAULT = new NamingStrategies.UnderScoreSeparatedLowerCase();

   @NonNull
   String mappedName(@NonNull String name);

   @NonNull
   default String mappedName(@NonNull PersistentEntity entity) {
      ArgumentUtils.requireNonNull("entity", entity);
      return (String)entity.getAnnotationMetadata()
         .stringValue(MappedEntity.class)
         .filter(StringUtils::isNotEmpty)
         .orElseGet(() -> this.mappedName(entity.getSimpleName()));
   }

   @NonNull
   default String mappedName(Embedded embedded, PersistentProperty property) {
      return this.mappedName(embedded.getName() + NameUtils.capitalize(property.getPersistedName()));
   }

   @NonNull
   default String mappedName(@NonNull PersistentProperty property) {
      ArgumentUtils.requireNonNull("property", property);
      return property instanceof Association
         ? this.mappedName((Association)property)
         : (String)property.getAnnotationMetadata()
            .stringValue(MappedProperty.class)
            .filter(StringUtils::isNotEmpty)
            .orElseGet(() -> this.mappedName(property.getName()));
   }

   @NonNull
   default String mappedName(Association association) {
      String providedName = (String)association.getAnnotationMetadata().stringValue(MappedProperty.class).orElse(null);
      if (providedName != null) {
         return providedName;
      } else if (association.isForeignKey()) {
         return this.mappedName(association.getOwner().getDecapitalizedName() + association.getAssociatedEntity().getSimpleName());
      } else {
         switch(association.getKind()) {
            case ONE_TO_ONE:
            case MANY_TO_ONE:
               return this.mappedName(association.getName() + this.getForeignKeySuffix());
            default:
               return this.mappedName(association.getName());
         }
      }
   }

   @NonNull
   default String mappedName(@NonNull List<Association> associations, @NonNull PersistentProperty property) {
      if (associations.isEmpty()) {
         return this.mappedName(property);
      } else {
         StringBuilder sb = new StringBuilder();
         Association foreignAssociation = null;

         for(Association association : associations) {
            if (association.getKind() != Relation.Kind.EMBEDDED && foreignAssociation == null) {
               foreignAssociation = association;
            }

            if (sb.length() > 0) {
               sb.append(NameUtils.capitalize(association.getName()));
            } else {
               sb.append(association.getName());
            }
         }

         if (foreignAssociation != null) {
            if (foreignAssociation.getAssociatedEntity() == property.getOwner() && foreignAssociation.getAssociatedEntity().getIdentity() == property) {
               String providedName = (String)foreignAssociation.getAnnotationMetadata().stringValue(MappedProperty.class).orElse(null);
               if (providedName != null) {
                  return providedName;
               }

               sb.append(this.getForeignKeySuffix());
               return this.mappedName(sb.toString());
            }

            if (foreignAssociation.isForeignKey()) {
               throw new IllegalStateException("Foreign association cannot be mapped!");
            }
         } else {
            String providedName = (String)property.getAnnotationMetadata().stringValue(MappedProperty.class).orElse(null);
            if (providedName != null) {
               return providedName;
            }
         }

         if (sb.length() > 0) {
            sb.append(NameUtils.capitalize(property.getName()));
         } else {
            sb.append(property.getName());
         }

         return this.mappedName(sb.toString());
      }
   }

   default String mappedJoinTableColumn(PersistentEntity associated, List<Association> associations, PersistentProperty property) {
      StringBuilder sb = new StringBuilder();
      sb.append(associated.getDecapitalizedName());

      for(Association association : associations) {
         sb.append(NameUtils.capitalize(association.getName()));
      }

      if (associations.isEmpty()) {
         sb.append(this.getForeignKeySuffix());
      } else {
         sb.append(NameUtils.capitalize(property.getName()));
      }

      return this.mappedName(sb.toString());
   }

   @NonNull
   default String getForeignKeySuffix() {
      return "Id";
   }
}
