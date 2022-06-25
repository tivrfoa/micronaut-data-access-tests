package io.micronaut.data.model.query;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.PersistentPropertyPath;
import io.micronaut.data.model.query.builder.QueryParameterBinding;

public interface BindingParameter {
   @NonNull
   QueryParameterBinding bind(@NonNull BindingParameter.BindingContext bindingContext);

   public interface BindingContext {
      static BindingParameter.BindingContext create() {
         return new BindingContextImpl();
      }

      @NonNull
      BindingParameter.BindingContext index(int index);

      @NonNull
      BindingParameter.BindingContext name(@Nullable String name);

      @NonNull
      BindingParameter.BindingContext incomingMethodParameterProperty(@Nullable PersistentPropertyPath propertyPath);

      @NonNull
      BindingParameter.BindingContext outgoingQueryParameterProperty(@Nullable PersistentPropertyPath propertyPath);

      @NonNull
      BindingParameter.BindingContext expandable();

      int getIndex();

      @Nullable
      String getName();

      @Nullable
      PersistentPropertyPath getIncomingMethodParameterProperty();

      @Nullable
      PersistentPropertyPath getOutgoingQueryParameterProperty();

      boolean isExpandable();
   }
}
