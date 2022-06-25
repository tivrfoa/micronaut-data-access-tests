package io.micronaut.runtime;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
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
public final class $ApplicationConfiguration$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.BootstrapContextCompatible",
         Collections.EMPTY_MAP,
         "io.micronaut.context.annotation.ConfigurationProperties",
         AnnotationUtil.mapOf("value", "micronaut.application"),
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("prefix", "micronaut.application"),
         "io.micronaut.context.annotation.Primary",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("value", "micronaut.application"),
         "javax.inject.Qualifier",
         Collections.EMPTY_MAP,
         "javax.inject.Scope",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("value", "micronaut.application"),
         "javax.inject.Qualifier",
         Collections.EMPTY_MAP,
         "javax.inject.Scope",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.BootstrapContextCompatible",
         Collections.EMPTY_MAP,
         "io.micronaut.context.annotation.ConfigurationProperties",
         AnnotationUtil.mapOf("value", "micronaut.application"),
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("prefix", "micronaut.application"),
         "io.micronaut.context.annotation.Primary",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties"),
         "javax.inject.Qualifier",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary"),
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
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_2());
   }

   public $ApplicationConfiguration$Definition$Reference() {
      super(
         "io.micronaut.runtime.ApplicationConfiguration",
         "io.micronaut.runtime.$ApplicationConfiguration$Definition",
         $ANNOTATION_METADATA,
         true,
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
      return new $ApplicationConfiguration$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ApplicationConfiguration$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return ApplicationConfiguration.class;
   }
}
