package io.micronaut.inject.beans.visitor;

import io.micronaut.core.annotation.NonNull;

public class MappedSuperClassIntrospectionMapper extends EntityIntrospectedAnnotationMapper {
   @NonNull
   @Override
   public String getName() {
      return "javax.persistence.MappedSuperclass";
   }
}
