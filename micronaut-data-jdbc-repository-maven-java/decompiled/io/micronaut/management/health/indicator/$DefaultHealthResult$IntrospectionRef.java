package io.micronaut.management.health.indicator;

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
public final class $DefaultHealthResult$IntrospectionRef extends AbstractBeanIntrospectionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.core.annotation.Introspected", Collections.EMPTY_MAP, "io.micronaut.core.annotation.ReflectiveAccess", Collections.EMPTY_MAP
      ),
      Collections.EMPTY_MAP,
      Collections.EMPTY_MAP,
      AnnotationUtil.mapOf(
         "io.micronaut.core.annotation.Introspected", Collections.EMPTY_MAP, "io.micronaut.core.annotation.ReflectiveAccess", Collections.EMPTY_MAP
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
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_2(),
         AnnotationUtil.mapOf(
            "as",
            $micronaut_load_class_value_3(),
            "builder",
            $micronaut_load_class_value_3(),
            "contentAs",
            $micronaut_load_class_value_3(),
            "contentConverter",
            $micronaut_load_class_value_4(),
            "contentUsing",
            $micronaut_load_class_value_5(),
            "converter",
            $micronaut_load_class_value_4(),
            "keyAs",
            $micronaut_load_class_value_3(),
            "keyUsing",
            $micronaut_load_class_value_6(),
            "using",
            $micronaut_load_class_value_5()
         )
      );
   }

   @Override
   public BeanIntrospection load() {
      return new $DefaultHealthResult$Introspection();
   }

   @Override
   public String getName() {
      return "io.micronaut.management.health.indicator.DefaultHealthResult";
   }

   @Override
   public Class getBeanType() {
      return DefaultHealthResult.class;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return $ANNOTATION_METADATA;
   }
}
