package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;

// $FF: synthetic class
@Generated
final class $DefaultDeleteAllReactiveInterceptor$Introspection extends AbstractInitializableBeanIntrospection {
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{
      Argument.of(
         RepositoryOperations.class,
         "operations",
         new DefaultAnnotationMetadata(
            AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            Collections.EMPTY_MAP,
            AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            false,
            true
         ),
         null
      )
   };

   public $DefaultDeleteAllReactiveInterceptor$Introspection() {
      super(
         DefaultDeleteAllReactiveInterceptor.class,
         $DefaultDeleteAllReactiveInterceptor$IntrospectionRef.$ANNOTATION_METADATA,
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
      return new DefaultDeleteAllReactiveInterceptor((RepositoryOperations)var1[0]);
   }
}
