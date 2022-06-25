package io.micronaut.data.model.query;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Named;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.PersistentPropertyPath;
import io.micronaut.data.model.query.builder.QueryParameterBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QueryParameter implements Named, BindingParameter {
   private final String name;

   public QueryParameter(@NonNull String name) {
      ArgumentUtils.requireNonNull("name", name);
      this.name = name;
   }

   @NonNull
   @Override
   public String getName() {
      return this.name;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         QueryParameter that = (QueryParameter)o;
         return this.name.equals(that.name);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.name});
   }

   @NonNull
   public static QueryParameter of(@NonNull String name) {
      return new QueryParameter(name);
   }

   @Override
   public QueryParameterBinding bind(BindingParameter.BindingContext bindingContext) {
      final String name = bindingContext.getName() == null ? String.valueOf(bindingContext.getIndex()) : bindingContext.getName();
      final PersistentPropertyPath outgoingQueryParameterProperty = bindingContext.getOutgoingQueryParameterProperty();
      return new QueryParameterBinding() {
         @Override
         public String getKey() {
            return name;
         }

         @Override
         public String[] getPropertyPath() {
            return QueryParameter.this.asStringPath(outgoingQueryParameterProperty.getAssociations(), outgoingQueryParameterProperty.getProperty());
         }

         @Override
         public DataType getDataType() {
            return outgoingQueryParameterProperty.getProperty().getDataType();
         }

         @Override
         public boolean isExpandable() {
            return bindingContext.isExpandable();
         }
      };
   }

   private String[] asStringPath(List<Association> associations, PersistentProperty property) {
      if (associations.isEmpty()) {
         return new String[]{property.getName()};
      } else {
         List<String> path = new ArrayList(associations.size() + 1);

         for(Association association : associations) {
            path.add(association.getName());
         }

         path.add(property.getName());
         return (String[])path.toArray(new String[0]);
      }
   }
}
