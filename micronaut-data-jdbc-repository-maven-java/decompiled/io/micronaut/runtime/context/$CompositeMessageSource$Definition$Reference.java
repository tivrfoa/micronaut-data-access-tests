package io.micronaut.runtime.context;

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
public final class $CompositeMessageSource$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.internMapOf("io.micronaut.context.annotation.Primary", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
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
         )
      ),
      AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary")),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
   }

   public $CompositeMessageSource$Definition$Reference() {
      super(
         "io.micronaut.runtime.context.CompositeMessageSource",
         "io.micronaut.runtime.context.$CompositeMessageSource$Definition",
         $ANNOTATION_METADATA,
         true,
         false,
         false,
         false,
         false,
         false,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $CompositeMessageSource$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $CompositeMessageSource$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return CompositeMessageSource.class;
   }
}
