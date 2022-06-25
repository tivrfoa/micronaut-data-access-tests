package io.micronaut.scheduling.io.watch;

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
public final class $WatchServiceFactory$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
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
      Map var0;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Factory",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("defaultValue", "true", "property", "micronaut.io.watch.enabled", "value", "true"),
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                  ),
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new FileWatchCondition())), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf(
                        "missing",
                        new String[]{"io.methvin.watchservice.MacOSXListeningWatchService"},
                        "missingClasses",
                        new String[]{"io.methvin.watchservice.MacOSXListeningWatchService"}
                     ),
                     var0
                  )
               }
            ),
            "jakarta.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_3())),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_3())),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Factory",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("defaultValue", "true", "property", "micronaut.io.watch.enabled", "value", "true"),
                     var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new FileWatchCondition())), var0
                  ),
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf(
                        "missing",
                        new String[]{"io.methvin.watchservice.MacOSXListeningWatchService"},
                        "missingClasses",
                        new String[]{"io.methvin.watchservice.MacOSXListeningWatchService"}
                     ),
                     var0
                  )
               }
            ),
            "jakarta.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
         false,
         true
      );
   }

   public $WatchServiceFactory$Definition$Reference() {
      super(
         "io.micronaut.scheduling.io.watch.WatchServiceFactory",
         "io.micronaut.scheduling.io.watch.$WatchServiceFactory$Definition",
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
      return new $WatchServiceFactory$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $WatchServiceFactory$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return WatchServiceFactory.class;
   }
}
