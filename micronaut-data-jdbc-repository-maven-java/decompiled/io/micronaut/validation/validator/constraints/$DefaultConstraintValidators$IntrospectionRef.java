package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.beans.AbstractBeanIntrospectionReference;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.core.beans.BeanIntrospectionReference"
)
public final class $DefaultConstraintValidators$IntrospectionRef extends AbstractBeanIntrospectionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf("io.micronaut.core.annotation.Introspected", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("io.micronaut.core.annotation.Introspected", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(),
         AnnotationUtil.mapOf(
            "accessKind",
            new String[]{"METHOD"},
            "annotationMetadata",
            true,
            "classes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "excludedAnnotations",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "excludes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "includedAnnotations",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "includes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "indexed",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "packages",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "visibility",
            new String[]{"DEFAULT"},
            "withPrefix",
            "with"
         )
      );
   }

   @Override
   public BeanIntrospection load() {
      return new $DefaultConstraintValidators$Introspection();
   }

   @Override
   public String getName() {
      return "io.micronaut.validation.validator.constraints.DefaultConstraintValidators";
   }

   @Override
   public Class getBeanType() {
      return DefaultConstraintValidators.class;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return $ANNOTATION_METADATA;
   }
}
