package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.BeanType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Internal
public class TypeAnnotationQualifier<T> implements Qualifier<T> {
   private final List<Class> types;

   TypeAnnotationQualifier(@Nullable Class<?>... types) {
      if (types != null) {
         this.types = new ArrayList(types.length);

         for(Class<?> type : types) {
            Type typeAnn = (Type)type.getAnnotation(Type.class);
            if (typeAnn != null) {
               this.types.addAll(Arrays.asList(typeAnn.value()));
            } else {
               this.types.add(type);
            }
         }
      } else {
         this.types = Collections.emptyList();
      }

   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      return candidates.filter(candidate -> this.areTypesCompatible(candidate.getBeanType()));
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TypeAnnotationQualifier<?> that = (TypeAnnotationQualifier)o;
         return this.types.equals(that.types);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.types.hashCode();
   }

   public String toString() {
      return "<" + (String)this.types.stream().map(Class::getSimpleName).collect(Collectors.joining("|")) + ">";
   }

   private boolean areTypesCompatible(Class type) {
      return this.types.stream().anyMatch(c -> c.isAssignableFrom(type));
   }
}
