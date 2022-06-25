package io.micronaut.data.runtime.event.listeners;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $UUIDGeneratingEntityEventListener$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
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
                  AnnotationUtil.mapOf("value", $micronaut_load_class_value_1()),
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
   }

   public $UUIDGeneratingEntityEventListener$Definition$Reference() {
      super(
         "io.micronaut.data.runtime.event.listeners.UUIDGeneratingEntityEventListener",
         "io.micronaut.data.runtime.event.listeners.$UUIDGeneratingEntityEventListener$Definition",
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
      return new $UUIDGeneratingEntityEventListener$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $UUIDGeneratingEntityEventListener$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return UUIDGeneratingEntityEventListener.class;
   }
}
