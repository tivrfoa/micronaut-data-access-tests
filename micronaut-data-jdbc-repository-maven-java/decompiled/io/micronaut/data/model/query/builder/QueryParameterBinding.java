package io.micronaut.data.model.query.builder;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.DataType;

@Internal
public interface QueryParameterBinding {
   String getKey();

   DataType getDataType();

   @Nullable
   default String getConverterClassName() {
      return null;
   }

   default int getParameterIndex() {
      return -1;
   }

   @Nullable
   default String[] getParameterBindingPath() {
      return null;
   }

   @Nullable
   default String[] getPropertyPath() {
      return null;
   }

   default boolean isAutoPopulated() {
      return false;
   }

   default boolean isRequiresPreviousPopulatedValue() {
      return false;
   }

   default boolean isExpandable() {
      return false;
   }
}
