package com.example;

import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.beans.AbstractBeanIntrospectionReference;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Map;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.core.beans.BeanIntrospectionReference"
)
public final class $Address$IntrospectionRef extends AbstractBeanIntrospectionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(), AnnotationUtil.mapOf("escape", true, "namingStrategy", $micronaut_load_class_value_1())
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_2(),
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
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_3());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_4(),
         AnnotationUtil.mapOf("converter", $micronaut_load_class_value_5(), "converterPersistedType", $micronaut_load_class_value_5(), "type", "OBJECT")
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_6(), AnnotationUtil.mapOf("cascade", new String[]{"NONE"}));
      Map var0;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "com.github.tivrfoa.mapresultset.api.Table",
            AnnotationUtil.mapOf("name", "address"),
            "io.micronaut.core.annotation.Introspected",
            AnnotationUtil.mapOf(
               "excludedAnnotations",
               $micronaut_load_class_value_7(),
               "indexed",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_3()),
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Introspected$IndexedAnnotation")
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_8()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_9()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_10()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_11()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_4(), "member", "value"),
                     var0
                  )
               }
            ),
            "io.micronaut.data.annotation.MappedEntity",
            AnnotationUtil.mapOf("value", "address")
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.core.annotation.Introspected",
            AnnotationUtil.mapOf(
               "excludedAnnotations",
               new AnnotationClassValue[]{$micronaut_load_class_value_7()},
               "indexed",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_3()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_8()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_9()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_10()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_4(), "member", "value"),
                     var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_11(), "member", "value"),
                     var0
                  )
               }
            )
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.core.annotation.Introspected",
            AnnotationUtil.mapOf(
               "excludedAnnotations",
               new AnnotationClassValue[]{$micronaut_load_class_value_7()},
               "indexed",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_3()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_8()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_9()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_10()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_4(), "member", "value"),
                     var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_11(), "member", "value"),
                     var0
                  )
               }
            )
         ),
         AnnotationUtil.mapOf(
            "com.github.tivrfoa.mapresultset.api.Table",
            AnnotationUtil.mapOf("name", "address"),
            "io.micronaut.core.annotation.Introspected",
            AnnotationUtil.mapOf(
               "excludedAnnotations",
               $micronaut_load_class_value_7(),
               "indexed",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_3()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_8()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_9()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_10()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_11()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_4(), "member", "value"),
                     var0
                  )
               }
            ),
            "io.micronaut.data.annotation.MappedEntity",
            AnnotationUtil.mapOf("value", "address")
         ),
         AnnotationUtil.mapOf("io.micronaut.core.annotation.Introspected", AnnotationUtil.internListOf("io.micronaut.data.annotation.MappedEntity")),
         false,
         true
      );
   }

   @Override
   public BeanIntrospection load() {
      return new $Address$Introspection();
   }

   @Override
   public String getName() {
      return "com.example.Address";
   }

   @Override
   public Class getBeanType() {
      return Address.class;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return $ANNOTATION_METADATA;
   }
}
