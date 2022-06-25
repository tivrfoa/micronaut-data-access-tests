package io.micronaut.scheduling.io.watch;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $WatchServiceFactory$WatchService0$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      Map var0;
      $ANNOTATION_METADATA = new AnnotationMetadataHierarchy(
         new DefaultAnnotationMetadata(
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
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new FileWatchCondition())),
                        var0
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
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
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
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new FileWatchCondition())),
                        var0
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
         ),
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               AnnotationUtil.mapOf("preDestroy", "close"),
               "io.micronaut.context.annotation.Primary",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Prototype",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf(
                           "missing",
                           new String[]{"io.methvin.watchservice.MacOSXListeningWatchService"},
                           "missingClasses",
                           new String[]{"io.methvin.watchservice.MacOSXListeningWatchService"}
                        ),
                        var0
                     ),
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("defaultValue", "true", "property", "micronaut.io.watch.enabled", "value", "true"),
                        var0
                     ),
                     new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "micronaut.io.watch.paths"), var0)
                  }
               )
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               Collections.EMPTY_MAP,
               "javax.inject.Qualifier",
               Collections.EMPTY_MAP,
               "javax.inject.Scope",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               Collections.EMPTY_MAP,
               "javax.inject.Qualifier",
               Collections.EMPTY_MAP,
               "javax.inject.Scope",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               AnnotationUtil.mapOf("preDestroy", "close"),
               "io.micronaut.context.annotation.Primary",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Prototype",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf(
                           "missing",
                           new String[]{"io.methvin.watchservice.MacOSXListeningWatchService"},
                           "missingClasses",
                           new String[]{"io.methvin.watchservice.MacOSXListeningWatchService"}
                        ),
                        var0
                     ),
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("defaultValue", "true", "property", "micronaut.io.watch.enabled", "value", "true"),
                        var0
                     ),
                     new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "micronaut.io.watch.paths"), var0)
                  }
               )
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.Prototype"),
               "javax.inject.Qualifier",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary"),
               "javax.inject.Scope",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.Prototype")
            ),
            false,
            true
         )
      );
   }

   public $WatchServiceFactory$WatchService0$Definition$Reference() {
      super(
         "java.nio.file.WatchService",
         "io.micronaut.scheduling.io.watch.$WatchServiceFactory$WatchService0$Definition",
         $ANNOTATION_METADATA,
         true,
         false,
         true,
         false,
         false,
         false,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $WatchServiceFactory$WatchService0$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $WatchServiceFactory$WatchService0$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return WatchService.class;
   }
}
