package io.micronaut.data.runtime.intercept;

import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;

// $FF: synthetic class
@Generated
final class $DefaultFindOneInterceptor$Introspection extends AbstractInitializableBeanIntrospection {
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{
      Argument.of(
         RepositoryOperations.class,
         "datastore",
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

   public $DefaultFindOneInterceptor$Introspection() {
      super(DefaultFindOneInterceptor.class, $DefaultFindOneInterceptor$IntrospectionRef.$ANNOTATION_METADATA, null, $CONSTRUCTOR_ARGUMENTS, null, null);
   }

   @Override
   public final int propertyIndexOf(String var1) {
      return -1;
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new DefaultFindOneInterceptor((RepositoryOperations)var1[0]);
   }
}
