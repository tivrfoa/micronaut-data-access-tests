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
public final class $Person$IntrospectionRef extends AbstractBeanIntrospectionReference {
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
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_4(), AnnotationUtil.mapOf("value", "AUTO"));
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_5(),
         AnnotationUtil.mapOf("converter", $micronaut_load_class_value_6(), "converterPersistedType", $micronaut_load_class_value_6(), "type", "OBJECT")
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_7(), AnnotationUtil.mapOf("cascade", new String[]{"NONE"}));
      Map var0;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.core.annotation.Introspected",
            AnnotationUtil.mapOf(
               "excludedAnnotations",
               $micronaut_load_class_value_8(),
               "indexed",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_3()),
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Introspected$IndexedAnnotation")
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
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_12()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_5(), "member", "value"),
                     var0
                  )
               }
            ),
            "io.micronaut.data.annotation.MappedEntity",
            AnnotationUtil.mapOf("value", "person")
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.core.annotation.Introspected",
            AnnotationUtil.mapOf(
               "excludedAnnotations",
               new AnnotationClassValue[]{$micronaut_load_class_value_8()},
               "indexed",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_3()), var0
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
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_5(), "member", "value"),
                     var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_12(), "member", "value"),
                     var0
                  )
               }
            )
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.core.annotation.Introspected",
            AnnotationUtil.mapOf(
               "excludedAnnotations",
               new AnnotationClassValue[]{$micronaut_load_class_value_8()},
               "indexed",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_3()), var0
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
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_5(), "member", "value"),
                     var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_12(), "member", "value"),
                     var0
                  )
               }
            )
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.core.annotation.Introspected",
            AnnotationUtil.mapOf(
               "excludedAnnotations",
               $micronaut_load_class_value_8(),
               "indexed",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_3()), var0
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
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation", AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_12()), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                     AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_5(), "member", "value"),
                     var0
                  )
               }
            ),
            "io.micronaut.data.annotation.MappedEntity",
            AnnotationUtil.mapOf("value", "person")
         ),
         AnnotationUtil.mapOf("io.micronaut.core.annotation.Introspected", AnnotationUtil.internListOf("io.micronaut.data.annotation.MappedEntity")),
         false,
         true
      );
   }

   @Override
   public BeanIntrospection load() {
      return new $Person$Introspection();
   }

   @Override
   public String getName() {
      return "com.example.Person";
   }

   @Override
   public Class getBeanType() {
      return Person.class;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return $ANNOTATION_METADATA;
   }
}
