package io.micronaut.http.hateoas;

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
public final class $GenericResource$IntrospectionRef extends AbstractBeanIntrospectionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.internMapOf("io.micronaut.core.annotation.Introspected", Collections.EMPTY_MAP),
      Collections.EMPTY_MAP,
      Collections.EMPTY_MAP,
      AnnotationUtil.mapOf(
         "io.micronaut.core.annotation.Introspected",
         Collections.EMPTY_MAP,
         "io.micronaut.http.annotation.Produces",
         AnnotationUtil.mapOf("value", new String[]{"application/hal+json"})
      ),
      Collections.EMPTY_MAP,
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
         $micronaut_load_class_value_1(), AnnotationUtil.mapOf("single", false, "value", new String[]{"application/json"})
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_2(), AnnotationUtil.mapOf("enabled", true));
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_3());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_4(), AnnotationUtil.mapOf("access", "AUTO", "index", -1, "required", false)
      );
   }

   @Override
   public BeanIntrospection load() {
      return new $GenericResource$Introspection();
   }

   @Override
   public String getName() {
      return "io.micronaut.http.hateoas.GenericResource";
   }

   @Override
   public Class getBeanType() {
      return GenericResource.class;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return $ANNOTATION_METADATA;
   }
}
