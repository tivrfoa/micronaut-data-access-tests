package io.micronaut.management.health.indicator.service;

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
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted$Definition$Reference
   extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      Map var0;
      Map var1;
      $ANNOTATION_METADATA = new AnnotationMetadataHierarchy(
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                        var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                     )
                  }
               ),
               "io.micronaut.management.health.indicator.annotation.Readiness",
               Collections.EMPTY_MAP,
               "javax.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                        var0
                     )
                  }
               ),
               "io.micronaut.management.health.indicator.annotation.Readiness",
               Collections.EMPTY_MAP,
               "javax.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "javax.inject.Qualifier",
               AnnotationUtil.internListOf("io.micronaut.management.health.indicator.annotation.Readiness"),
               "javax.inject.Scope",
               AnnotationUtil.internListOf("javax.inject.Singleton")
            ),
            false,
            true
         ),
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Adapter",
               AnnotationUtil.mapOf(
                  "adaptedArgumentTypes",
                  new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                  "adaptedBean",
                  new AnnotationClassValue[]{$micronaut_load_class_value_2()},
                  "adaptedMethod",
                  "onServiceStarted"
               ),
               "io.micronaut.runtime.event.annotation.EventListener",
               Collections.EMPTY_MAP,
               "jakarta.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Adapter",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
               "io.micronaut.context.annotation.DefaultScope",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_4()),
               "io.micronaut.context.annotation.Executable",
               Collections.EMPTY_MAP,
               "io.micronaut.core.annotation.Indexes",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.core.annotation.Indexed",
                        AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
                        var1 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                     )
                  }
               )
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Adapter",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
               "io.micronaut.context.annotation.DefaultScope",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_4()),
               "io.micronaut.context.annotation.Executable",
               Collections.EMPTY_MAP,
               "io.micronaut.core.annotation.Indexes",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue("io.micronaut.core.annotation.Indexed", AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()), var1)
                  }
               )
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Adapter",
               AnnotationUtil.mapOf(
                  "adaptedArgumentTypes",
                  new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                  "adaptedBean",
                  new AnnotationClassValue[]{$micronaut_load_class_value_2()},
                  "adaptedMethod",
                  "onServiceStarted"
               ),
               "io.micronaut.runtime.event.annotation.EventListener",
               Collections.EMPTY_MAP,
               "jakarta.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Adapter",
               AnnotationUtil.internListOf("io.micronaut.runtime.event.annotation.EventListener"),
               "io.micronaut.context.annotation.DefaultScope",
               AnnotationUtil.internListOf("io.micronaut.aop.Adapter"),
               "io.micronaut.context.annotation.Executable",
               AnnotationUtil.internListOf("io.micronaut.aop.Adapter"),
               "io.micronaut.core.annotation.Indexes",
               AnnotationUtil.internListOf("io.micronaut.runtime.event.annotation.EventListener")
            ),
            false,
            true
         )
      );
   }

   public $ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted$Definition$Reference() {
      super(
         "io.micronaut.management.health.indicator.service.ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted",
         "io.micronaut.management.health.indicator.service.$ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted$Definition",
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
      return new $ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted.class;
   }
}
