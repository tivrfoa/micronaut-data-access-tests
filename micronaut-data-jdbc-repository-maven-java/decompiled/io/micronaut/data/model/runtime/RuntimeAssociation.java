package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.type.Argument;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.exceptions.MappingException;
import io.micronaut.data.model.Association;
import java.util.Optional;

public class RuntimeAssociation<T> extends RuntimePersistentProperty<T> implements Association {
   private final Relation.Kind kind = Association.super.getKind();
   private final String aliasName = Association.super.getAliasName();
   private final boolean isForeignKey = Association.super.isForeignKey();

   RuntimeAssociation(RuntimePersistentEntity<T> owner, BeanProperty<T, ?> property, boolean constructorArg) {
      super(owner, property, constructorArg);
   }

   @Override
   public boolean isForeignKey() {
      return this.isForeignKey;
   }

   @Override
   public String getAliasName() {
      return this.aliasName;
   }

   @NonNull
   @Override
   public Relation.Kind getKind() {
      return this.kind;
   }

   @Override
   public boolean isRequired() {
      return !this.isForeignKey() && super.isRequired();
   }

   @Override
   public Optional<RuntimeAssociation<?>> getInverseSide() {
      return Association.super.getInverseSide();
   }

   @NonNull
   public RuntimePersistentEntity<?> getAssociatedEntity() {
      switch(this.getKind()) {
         case ONE_TO_MANY:
         case MANY_TO_MANY:
            Argument<?> typeArg = (Argument)this.getProperty().asArgument().getFirstTypeVariable().orElse(null);
            if (typeArg != null) {
               return this.getOwner().getEntity(typeArg.getType());
            }

            throw new MappingException(
               "Collection association [" + this.getName() + "] of entity [" + this.getOwner().getName() + "] does not specify a generic type argument"
            );
         default:
            return this.getOwner().getEntity(this.getProperty().getType());
      }
   }
}
