package io.micronaut.runtime.converters.time;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $TimeConverterRegistrar$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(),
         AnnotationUtil.mapOf(
            "beans",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "classes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "condition",
            $micronaut_load_class_value_1(),
            "entities",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "env",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "missing",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "missingBeans",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "missingClasses",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "missingConfigurations",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "notEnv",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "notOs",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "os",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "resources",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "sdk",
            "MICRONAUT"
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_2());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_3(),
         AnnotationUtil.mapOf(
            "accessType", new String[]{"ALL_DECLARED_CONSTRUCTORS"}, "typeNames", ArrayUtils.EMPTY_OBJECT_ARRAY, "value", ArrayUtils.EMPTY_OBJECT_ARRAY
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_4());
      Map var0;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("notEnv", new String[]{"android"}),
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                  )
               }
            ),
            "io.micronaut.core.annotation.TypeHint",
            AnnotationUtil.mapOf(
               "accessType",
               new String[]{"ALL_PUBLIC"},
               "value",
               new AnnotationClassValue[]{
                  $micronaut_load_class_value_5(),
                  $micronaut_load_class_value_6(),
                  $micronaut_load_class_value_7(),
                  $micronaut_load_class_value_8(),
                  $micronaut_load_class_value_9(),
                  $micronaut_load_class_value_10(),
                  $micronaut_load_class_value_11(),
                  $micronaut_load_class_value_12(),
                  $micronaut_load_class_value_13(),
                  $micronaut_load_class_value_14(),
                  $micronaut_load_class_value_15(),
                  $micronaut_load_class_value_16(),
                  $micronaut_load_class_value_17(),
                  $micronaut_load_class_value_18()
               }
            ),
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("notEnv", new String[]{"android"}), var0)
               }
            ),
            "io.micronaut.core.annotation.Indexes",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Indexed",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_19()),
                     AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                  )
               }
            ),
            "io.micronaut.core.annotation.TypeHint",
            AnnotationUtil.mapOf(
               "accessType",
               new String[]{"ALL_PUBLIC"},
               "value",
               new AnnotationClassValue[]{
                  $micronaut_load_class_value_5(),
                  $micronaut_load_class_value_6(),
                  $micronaut_load_class_value_7(),
                  $micronaut_load_class_value_8(),
                  $micronaut_load_class_value_9(),
                  $micronaut_load_class_value_10(),
                  $micronaut_load_class_value_11(),
                  $micronaut_load_class_value_12(),
                  $micronaut_load_class_value_13(),
                  $micronaut_load_class_value_14(),
                  $micronaut_load_class_value_15(),
                  $micronaut_load_class_value_16(),
                  $micronaut_load_class_value_17(),
                  $micronaut_load_class_value_18()
               }
            ),
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
         false,
         true
      );
   }

   public $TimeConverterRegistrar$Definition$Reference() {
      super(
         "io.micronaut.runtime.converters.time.TimeConverterRegistrar",
         "io.micronaut.runtime.converters.time.$TimeConverterRegistrar$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         true,
         false,
         true,
         false,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $TimeConverterRegistrar$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $TimeConverterRegistrar$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return TimeConverterRegistrar.class;
   }
}
