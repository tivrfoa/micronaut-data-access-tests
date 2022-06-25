package io.micronaut.management.health.monitor;

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
public final class $HealthMonitorTask$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
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
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_2(), AnnotationUtil.mapOf("scheduler", "scheduled"));
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_3(), AnnotationUtil.mapOf("processOnStartup", false));
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_4(), AnnotationUtil.mapOf("shutdownOnError", true));
      DefaultAnnotationMetadata.registerRepeatableAnnotations(
         AnnotationUtil.mapOf("io.micronaut.scheduling.annotation.Scheduled", "io.micronaut.scheduling.annotation.Schedules")
      );
      Map var0;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_5()}),
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                  ),
                  new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "micronaut.application.name"), var0),
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("defaultValue", "true", "property", "micronaut.health.monitor.enabled", "value", "true"),
                     var0
                  )
               }
            ),
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_5()}),
                     var0
                  ),
                  new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "micronaut.application.name"), var0),
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("defaultValue", "true", "property", "micronaut.health.monitor.enabled", "value", "true"),
                     var0
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
   }

   public $HealthMonitorTask$Definition$Reference() {
      super(
         "io.micronaut.management.health.monitor.HealthMonitorTask",
         "io.micronaut.management.health.monitor.$HealthMonitorTask$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         true,
         false,
         true,
         false,
         false,
         true
      );
   }

   @Override
   public BeanDefinition load() {
      return new $HealthMonitorTask$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $HealthMonitorTask$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return HealthMonitorTask.class;
   }
}
