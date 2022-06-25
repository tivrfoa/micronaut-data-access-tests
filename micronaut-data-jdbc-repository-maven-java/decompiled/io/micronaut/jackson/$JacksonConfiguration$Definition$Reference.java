package io.micronaut.jackson;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $JacksonConfiguration$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationProperties",
         AnnotationUtil.mapOf("value", "jackson"),
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("prefix", "jackson"),
         "io.micronaut.core.annotation.TypeHint",
         AnnotationUtil.mapOf(
            "value",
            new AnnotationClassValue[]{
               $micronaut_load_class_value_4(), $micronaut_load_class_value_5(), $micronaut_load_class_value_6(), $micronaut_load_class_value_7()
            }
         )
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("value", "jackson"),
         "javax.inject.Scope",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("value", "jackson"),
         "javax.inject.Scope",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationProperties",
         AnnotationUtil.mapOf("value", "jackson"),
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("prefix", "jackson"),
         "io.micronaut.core.annotation.Internal",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.TypeHint",
         AnnotationUtil.mapOf(
            "value",
            new AnnotationClassValue[]{
               $micronaut_load_class_value_4(), $micronaut_load_class_value_5(), $micronaut_load_class_value_6(), $micronaut_load_class_value_7()
            }
         )
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties"),
         "javax.inject.Scope",
         AnnotationUtil.internListOf("javax.inject.Singleton"),
         "javax.inject.Singleton",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties")
      ),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(),
         AnnotationUtil.mapOf("cliPrefix", ArrayUtils.EMPTY_OBJECT_ARRAY, "excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY)
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_1(), AnnotationUtil.mapOf("excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY)
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_2(),
         AnnotationUtil.mapOf(
            "accessType", new String[]{"ALL_DECLARED_CONSTRUCTORS"}, "typeNames", ArrayUtils.EMPTY_OBJECT_ARRAY, "value", ArrayUtils.EMPTY_OBJECT_ARRAY
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_3());
   }

   public $JacksonConfiguration$Definition$Reference() {
      super(
         "io.micronaut.jackson.JacksonConfiguration",
         "io.micronaut.jackson.$JacksonConfiguration$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         false,
         false,
         true,
         true,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $JacksonConfiguration$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $JacksonConfiguration$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return JacksonConfiguration.class;
   }
}
