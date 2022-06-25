package io.micronaut.flyway;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.AdvisedBeanType;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $DataSourceMigrationRunner$Definition$Intercepted$Definition$Reference
   extends AbstractInitializableBeanDefinitionReference
   implements AdvisedBeanType {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.internMapOf("javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "io.micronaut.core.annotation.Indexes",
         AnnotationUtil.mapOf(
            "value",
            new AnnotationValue[]{
               new AnnotationValue(
                  "io.micronaut.core.annotation.Indexed",
                  AnnotationUtil.mapOf("value", $micronaut_load_class_value_5()),
                  AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
               )
            }
         ),
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_2(), AnnotationUtil.mapOf("value", "scheduled"));
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_3(), AnnotationUtil.mapOf("processOnStartup", false));
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_4(),
         AnnotationUtil.mapOf("cacheableLazyTarget", false, "hotswap", false, "lazy", false, "proxyTarget", false, "proxyTargetMode", "ERROR")
      );
      DefaultAnnotationMetadata.registerRepeatableAnnotations(
         AnnotationUtil.mapOf("io.micronaut.aop.InterceptorBinding", "io.micronaut.aop.InterceptorBindingDefinitions")
      );
   }

   public $DataSourceMigrationRunner$Definition$Intercepted$Definition$Reference() {
      super(
         "io.micronaut.flyway.$DataSourceMigrationRunner$Definition$Intercepted",
         "io.micronaut.flyway.$DataSourceMigrationRunner$Definition$Intercepted$Definition",
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
      return new $DataSourceMigrationRunner$Definition$Intercepted$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $DataSourceMigrationRunner$Definition$Intercepted$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return $DataSourceMigrationRunner$Definition$Intercepted.class;
   }

   @Override
   public Class getInterceptedType() {
      return DataSourceMigrationRunner.class;
   }
}
