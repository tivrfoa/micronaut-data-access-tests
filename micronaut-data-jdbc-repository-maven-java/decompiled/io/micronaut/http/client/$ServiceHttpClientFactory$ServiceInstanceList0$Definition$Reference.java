package io.micronaut.http.client;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.discovery.StaticServiceInstanceList;
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
public final class $ServiceHttpClientFactory$ServiceInstanceList0$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      Map var0;
      $ANNOTATION_METADATA = new AnnotationMetadataHierarchy(
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Factory",
               Collections.EMPTY_MAP,
               "io.micronaut.core.annotation.Internal",
               Collections.EMPTY_MAP,
               "jakarta.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Factory",
               Collections.EMPTY_MAP,
               "io.micronaut.core.annotation.Internal",
               Collections.EMPTY_MAP,
               "jakarta.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
            false,
            true
         ),
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.EachBean",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_1()),
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new ServiceHttpClientCondition())),
                        var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                     )
                  }
               )
            ),
            AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.EachBean",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_1()),
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new ServiceHttpClientCondition())),
                        var0
                     )
                  }
               )
            ),
            AnnotationUtil.mapOf(
               "javax.inject.Scope",
               AnnotationUtil.internListOf("javax.inject.Singleton"),
               "javax.inject.Singleton",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.EachBean")
            ),
            false,
            true
         )
      );
   }

   public $ServiceHttpClientFactory$ServiceInstanceList0$Definition$Reference() {
      super(
         "io.micronaut.discovery.StaticServiceInstanceList",
         "io.micronaut.http.client.$ServiceHttpClientFactory$ServiceInstanceList0$Definition",
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
      return new $ServiceHttpClientFactory$ServiceInstanceList0$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ServiceHttpClientFactory$ServiceInstanceList0$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return StaticServiceInstanceList.class;
   }
}
