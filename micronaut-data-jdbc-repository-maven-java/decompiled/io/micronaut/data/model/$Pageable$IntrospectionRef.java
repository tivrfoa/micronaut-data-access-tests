package io.micronaut.data.model;

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
public final class $Pageable$IntrospectionRef extends AbstractBeanIntrospectionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "com.fasterxml.jackson.annotation.JsonIgnoreProperties",
         AnnotationUtil.mapOf("ignoreUnknown", true),
         "io.micronaut.core.annotation.Introspected",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "com.fasterxml.jackson.annotation.JsonIgnoreProperties",
         AnnotationUtil.mapOf("ignoreUnknown", true),
         "io.micronaut.core.annotation.Introspected",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonIgnoreProperties")
      ),
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
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_1(),
         AnnotationUtil.mapOf("allowGetters", false, "allowSetters", false, "ignoreUnknown", false, "value", ArrayUtils.EMPTY_OBJECT_ARRAY)
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_2());
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_3(), AnnotationUtil.mapOf("value", true));
   }

   @Override
   public BeanIntrospection load() {
      return new $Pageable$Introspection();
   }

   @Override
   public String getName() {
      return "io.micronaut.data.model.Pageable";
   }

   @Override
   public Class getBeanType() {
      return Pageable.class;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return $ANNOTATION_METADATA;
   }
}
