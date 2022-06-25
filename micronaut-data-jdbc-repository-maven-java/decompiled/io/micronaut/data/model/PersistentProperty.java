package io.micronaut.data.model;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.runtime.convert.AttributeConverter;

public interface PersistentProperty extends PersistentElement {
   @NonNull
   @Override
   String getName();

   @NonNull
   default String getCapitilizedName() {
      return NameUtils.capitalize(this.getName());
   }

   @NonNull
   String getTypeName();

   @NonNull
   PersistentEntity getOwner();

   default boolean isOptional() {
      return isNullableMetadata(this.getAnnotationMetadata());
   }

   default boolean isRequired() {
      return !this.isOptional() && !this.isGenerated() && !this.getAnnotationMetadata().hasStereotype(AutoPopulated.class);
   }

   default boolean isReadOnly() {
      return this.isGenerated();
   }

   default boolean isConstructorArgument() {
      return false;
   }

   default boolean isGenerated() {
      return this.getAnnotationMetadata().hasAnnotation(GeneratedValue.class);
   }

   default boolean isAutoPopulated() {
      return !this.isGenerated() && this.getAnnotationMetadata().hasStereotype(AutoPopulated.class);
   }

   boolean isAssignable(@NonNull String type);

   default boolean isAssignable(@NonNull Class<?> type) {
      return this.isAssignable(type.getName());
   }

   default DataType getDataType() {
      if (this instanceof Association) {
         return DataType.ENTITY;
      } else {
         AnnotationMetadata annotationMetadata = this.getAnnotationMetadata();
         return (DataType)annotationMetadata.enumValue(MappedProperty.class, "type", DataType.class).orElseGet(() -> {
            DataType dt = (DataType)annotationMetadata.enumValue(TypeDef.class, "type", DataType.class).orElse(null);
            if (dt != null) {
               return dt;
            } else {
               return this.isEnum() ? DataType.STRING : DataType.OBJECT;
            }
         });
      }
   }

   default boolean isEnum() {
      return false;
   }

   @Nullable
   default AttributeConverter<Object, Object> getConverter() {
      return null;
   }

   static boolean isNullableMetadata(@NonNull AnnotationMetadata metadata) {
      return metadata.getDeclaredAnnotationNames().stream().anyMatch(n -> NameUtils.getSimpleName(n).equalsIgnoreCase("nullable"));
   }
}
