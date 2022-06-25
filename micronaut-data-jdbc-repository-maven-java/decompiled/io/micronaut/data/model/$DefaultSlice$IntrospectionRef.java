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
public final class $DefaultSlice$IntrospectionRef extends AbstractBeanIntrospectionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.internMapOf("io.micronaut.core.annotation.Introspected", Collections.EMPTY_MAP),
      Collections.EMPTY_MAP,
      Collections.EMPTY_MAP,
      AnnotationUtil.internMapOf("io.micronaut.core.annotation.Introspected", Collections.EMPTY_MAP),
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
         $micronaut_load_class_value_1(),
         AnnotationUtil.mapOf("allowGetters", false, "allowSetters", false, "ignoreUnknown", false, "value", ArrayUtils.EMPTY_OBJECT_ARRAY)
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_2(),
         AnnotationUtil.mapOf(
            "accessType", new String[]{"ALL_DECLARED_CONSTRUCTORS"}, "typeNames", ArrayUtils.EMPTY_OBJECT_ARRAY, "value", ArrayUtils.EMPTY_OBJECT_ARRAY
         )
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_3(),
         AnnotationUtil.mapOf(
            "as",
            $micronaut_load_class_value_4(),
            "builder",
            $micronaut_load_class_value_4(),
            "contentAs",
            $micronaut_load_class_value_4(),
            "contentConverter",
            $micronaut_load_class_value_5(),
            "contentUsing",
            $micronaut_load_class_value_6(),
            "converter",
            $micronaut_load_class_value_5(),
            "keyAs",
            $micronaut_load_class_value_4(),
            "keyUsing",
            $micronaut_load_class_value_7(),
            "using",
            $micronaut_load_class_value_6()
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_8());
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_9(), AnnotationUtil.mapOf("value", true));
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_10());
   }

   @Override
   public BeanIntrospection load() {
      return new $DefaultSlice$Introspection();
   }

   @Override
   public String getName() {
      return "io.micronaut.data.model.DefaultSlice";
   }

   @Override
   public Class getBeanType() {
      return DefaultSlice.class;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return $ANNOTATION_METADATA;
   }
}
