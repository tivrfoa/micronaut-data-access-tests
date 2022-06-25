package io.micronaut.data.runtime.intercept.criteria.reactive;

import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;

// $FF: synthetic class
@Generated
final class $DeleteAllReactiveSpecificationInterceptor$Introspection extends AbstractInitializableBeanIntrospection {
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{Argument.of(RepositoryOperations.class, "operations")};

   public $DeleteAllReactiveSpecificationInterceptor$Introspection() {
      super(
         DeleteAllReactiveSpecificationInterceptor.class,
         $DeleteAllReactiveSpecificationInterceptor$IntrospectionRef.$ANNOTATION_METADATA,
         null,
         $CONSTRUCTOR_ARGUMENTS,
         null,
         null
      );
   }

   @Override
   public final int propertyIndexOf(String var1) {
      return -1;
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new DeleteAllReactiveSpecificationInterceptor((RepositoryOperations)var1[0]);
   }
}
