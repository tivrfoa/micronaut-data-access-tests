package io.micronaut.health;

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
public final class $HeartbeatTask$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_1(),
         AnnotationUtil.mapOf(
            "beans",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "classes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "condition",
            $micronaut_load_class_value_2(),
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
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_3());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_4());
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_5(), AnnotationUtil.mapOf("scheduler", "scheduled"));
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_6(), AnnotationUtil.mapOf("processOnStartup", false));
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_7(), AnnotationUtil.mapOf("shutdownOnError", true));
      DefaultAnnotationMetadata.registerRepeatableAnnotations(
         AnnotationUtil.mapOf("io.micronaut.scheduling.annotation.Scheduled", "io.micronaut.scheduling.annotation.Schedules")
      );
      Map var0;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf("io.micronaut.health.HeartbeatEnabled", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("notEnv", new String[]{"android", "function"}),
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                  ),
                  new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "micronaut.application.name"), var0),
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new HeartbeatDiscoveryClientCondition())),
                     var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_8()}),
                     var0
                  )
               }
            ),
            "javax.inject.Scope",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("notEnv", new String[]{"android", "function"}), var0),
                  new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "micronaut.application.name"), var0),
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new HeartbeatDiscoveryClientCondition())),
                     var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_8()}),
                     var0
                  )
               }
            ),
            "javax.inject.Scope",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.core.annotation.Indexes",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Indexed",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_9()),
                     AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                  )
               }
            ),
            "io.micronaut.health.HeartbeatEnabled",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.internListOf("io.micronaut.health.HeartbeatEnabled"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton")
         ),
         false,
         true
      );
   }

   public $HeartbeatTask$Definition$Reference() {
      super(
         "io.micronaut.health.HeartbeatTask",
         "io.micronaut.health.$HeartbeatTask$Definition",
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
      return new $HeartbeatTask$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $HeartbeatTask$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return HeartbeatTask.class;
   }
}
