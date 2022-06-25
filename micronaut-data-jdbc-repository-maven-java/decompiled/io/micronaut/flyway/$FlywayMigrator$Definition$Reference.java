package io.micronaut.flyway;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $FlywayMigrator$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.internMapOf("javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_0(), AnnotationUtil.mapOf("value", "scheduled"));
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_1(), AnnotationUtil.mapOf("processOnStartup", false));
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_2(),
         AnnotationUtil.mapOf("cacheableLazyTarget", false, "hotswap", false, "lazy", false, "proxyTarget", false, "proxyTargetMode", "ERROR")
      );
      DefaultAnnotationMetadata.registerRepeatableAnnotations(
         AnnotationUtil.mapOf("io.micronaut.aop.InterceptorBinding", "io.micronaut.aop.InterceptorBindingDefinitions")
      );
   }

   public $FlywayMigrator$Definition$Reference() {
      super(
         "io.micronaut.flyway.FlywayMigrator",
         "io.micronaut.flyway.$FlywayMigrator$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         false,
         false,
         true,
         false,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $FlywayMigrator$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $FlywayMigrator$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return FlywayMigrator.class;
   }
}
