package io.micronaut.scheduling.executor;

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
public final class $UserExecutorConfiguration$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("prefix", "micronaut.executors.*"),
         "io.micronaut.context.annotation.EachProperty",
         AnnotationUtil.mapOf("value", "micronaut.executors")
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("value", "micronaut.executors"),
         "javax.inject.Scope",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("value", "micronaut.executors"),
         "javax.inject.Scope",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("prefix", "micronaut.executors.*"),
         "io.micronaut.context.annotation.EachProperty",
         AnnotationUtil.mapOf("value", "micronaut.executors")
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.EachProperty"),
         "javax.inject.Scope",
         AnnotationUtil.internListOf("javax.inject.Singleton"),
         "javax.inject.Singleton",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.EachProperty")
      ),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(),
         AnnotationUtil.mapOf("excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY, "list", false)
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_1(), AnnotationUtil.mapOf("excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY)
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_2());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_3());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_4());
   }

   public $UserExecutorConfiguration$Definition$Reference() {
      super(
         "io.micronaut.scheduling.executor.UserExecutorConfiguration",
         "io.micronaut.scheduling.executor.$UserExecutorConfiguration$Definition",
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
      return new $UserExecutorConfiguration$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $UserExecutorConfiguration$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return UserExecutorConfiguration.class;
   }
}
