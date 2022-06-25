package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.DataType;

public interface QueryParameterBinding {
   @Nullable
   String getName();

   @NonNull
   default String getRequiredName() {
      String name = this.getName();
      if (name == null) {
         throw new IllegalStateException("Parameter name cannot be null for a query parameter: " + this);
      } else {
         return name;
      }
   }

   @Nullable
   DataType getDataType();

   @Nullable
   Class<?> getParameterConverterClass();

   int getParameterIndex();

   @Nullable
   String[] getParameterBindingPath();

   @Nullable
   String[] getPropertyPath();

   @NonNull
   default String[] getRequiredPropertyPath() {
      String[] propertyPath = this.getPropertyPath();
      if (propertyPath == null) {
         throw new IllegalStateException("Property path cannot be null for a query parameter: " + this);
      } else {
         return propertyPath;
      }
   }

   boolean isAutoPopulated();

   boolean isRequiresPreviousPopulatedValue();

   @Nullable
   QueryParameterBinding getPreviousPopulatedValueParameter();

   default boolean isExpandable() {
      return false;
   }
}
