package io.micronaut.data.runtime.query.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import java.util.Arrays;
import java.util.List;

@Internal
public final class StoredQueryParameter implements QueryParameterBinding {
   private final String name;
   private final DataType dataType;
   private final int parameterIndex;
   private final String[] parameterBindingPath;
   private final String[] propertyPath;
   private final boolean autoPopulated;
   private final boolean requiresPreviousPopulatedValue;
   private final Class<?> parameterConverterClass;
   private final boolean expandable;
   private final List<? extends QueryParameterBinding> all;
   private boolean previousInitialized;
   private QueryParameterBinding previousPopulatedValueParameter;

   StoredQueryParameter(
      String name,
      DataType dataType,
      int parameterIndex,
      String[] parameterBindingPath,
      String[] propertyPath,
      boolean autoPopulated,
      boolean requiresPreviousPopulatedValue,
      Class<?> parameterConverterClass,
      boolean expandable,
      List<? extends QueryParameterBinding> all
   ) {
      this.name = name;
      this.dataType = dataType;
      this.parameterIndex = parameterIndex;
      this.parameterBindingPath = parameterBindingPath;
      this.propertyPath = propertyPath;
      this.autoPopulated = autoPopulated;
      this.requiresPreviousPopulatedValue = requiresPreviousPopulatedValue;
      this.parameterConverterClass = parameterConverterClass;
      this.expandable = expandable;
      this.all = all;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public DataType getDataType() {
      return this.dataType;
   }

   @Override
   public Class<?> getParameterConverterClass() {
      return this.parameterConverterClass;
   }

   @Override
   public int getParameterIndex() {
      return this.parameterIndex;
   }

   @Override
   public String[] getParameterBindingPath() {
      return this.parameterBindingPath;
   }

   @Override
   public String[] getPropertyPath() {
      return this.propertyPath;
   }

   @Override
   public boolean isAutoPopulated() {
      return this.autoPopulated;
   }

   @Override
   public boolean isRequiresPreviousPopulatedValue() {
      return this.requiresPreviousPopulatedValue;
   }

   @Override
   public QueryParameterBinding getPreviousPopulatedValueParameter() {
      if (!this.previousInitialized) {
         for(QueryParameterBinding it : this.all) {
            if (it != this && it.getParameterIndex() != -1 && Arrays.equals(this.propertyPath, it.getPropertyPath())) {
               this.previousPopulatedValueParameter = it;
               break;
            }
         }

         this.previousInitialized = true;
      }

      return this.previousPopulatedValueParameter;
   }

   @Override
   public boolean isExpandable() {
      return this.expandable;
   }

   public String toString() {
      return "StoredQueryParameter{name='"
         + this.name
         + '\''
         + ", dataType="
         + this.dataType
         + ", parameterIndex="
         + this.parameterIndex
         + ", parameterBindingPath="
         + Arrays.toString(this.parameterBindingPath)
         + ", propertyPath="
         + Arrays.toString(this.propertyPath)
         + ", autoPopulated="
         + this.autoPopulated
         + ", requiresPreviousPopulatedValue="
         + this.requiresPreviousPopulatedValue
         + ", previousPopulatedValueParameter="
         + this.previousPopulatedValueParameter
         + ", expandable="
         + this.expandable
         + '}';
   }
}
