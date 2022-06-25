package io.micronaut.inject.beans.visitor.jakarta.persistence;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;

@Internal
public final class JakartaMappedSuperClassIntrospectionMapper extends JakartaEntityIntrospectedAnnotationMapper {
   @NonNull
   @Override
   public String getName() {
      return "jakarta.persistence.MappedSuperclass";
   }
}
