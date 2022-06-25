package io.micronaut.runtime.context.env;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $ConfigurationIntroductionAdvice$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.BootstrapContextCompatible",
         Collections.EMPTY_MAP,
         "io.micronaut.context.annotation.Prototype",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.Internal",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("io.micronaut.context.annotation.Bean", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("io.micronaut.context.annotation.Bean", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.BootstrapContextCompatible",
         Collections.EMPTY_MAP,
         "io.micronaut.context.annotation.Prototype",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.Indexes",
         AnnotationUtil.mapOf(
            "value",
            new AnnotationValue[]{
               new AnnotationValue(
                  "io.micronaut.core.annotation.Indexed",
                  AnnotationUtil.mapOf("value", $micronaut_load_class_value_4()),
                  AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
               )
            }
         ),
         "io.micronaut.core.annotation.Internal",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.Bean",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.Prototype"),
         "javax.inject.Scope",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.Prototype")
      ),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_2(), AnnotationUtil.mapOf("typed", ArrayUtils.EMPTY_OBJECT_ARRAY));
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_3());
   }

   public $ConfigurationIntroductionAdvice$Definition$Reference() {
      super(
         "io.micronaut.runtime.context.env.ConfigurationIntroductionAdvice",
         "io.micronaut.runtime.context.env.$ConfigurationIntroductionAdvice$Definition",
         $ANNOTATION_METADATA,
         false,
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
      return new $ConfigurationIntroductionAdvice$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ConfigurationIntroductionAdvice$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return ConfigurationIntroductionAdvice.class;
   }
}
