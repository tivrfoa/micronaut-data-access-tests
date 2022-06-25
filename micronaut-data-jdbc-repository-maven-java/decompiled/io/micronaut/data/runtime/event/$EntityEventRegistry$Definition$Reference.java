package io.micronaut.data.runtime.event;

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
public final class $EntityEventRegistry$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf("io.micronaut.context.annotation.Primary", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.Primary",
         Collections.EMPTY_MAP,
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
      AnnotationUtil.mapOf(
         "javax.inject.Qualifier",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary"),
         "javax.inject.Scope",
         AnnotationUtil.internListOf("javax.inject.Singleton")
      ),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
   }

   public $EntityEventRegistry$Definition$Reference() {
      super(
         "io.micronaut.data.runtime.event.EntityEventRegistry",
         "io.micronaut.data.runtime.event.$EntityEventRegistry$Definition",
         $ANNOTATION_METADATA,
         true,
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
      return new $EntityEventRegistry$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $EntityEventRegistry$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return EntityEventRegistry.class;
   }
}
