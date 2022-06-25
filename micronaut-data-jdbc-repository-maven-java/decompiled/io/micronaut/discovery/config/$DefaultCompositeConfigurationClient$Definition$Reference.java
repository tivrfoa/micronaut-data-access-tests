package io.micronaut.discovery.config;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $DefaultCompositeConfigurationClient$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.BootstrapContextCompatible",
         Collections.EMPTY_MAP,
         "io.micronaut.context.annotation.Primary",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.BootstrapContextCompatible",
         Collections.EMPTY_MAP,
         "io.micronaut.context.annotation.Primary",
         Collections.EMPTY_MAP,
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

   public $DefaultCompositeConfigurationClient$Definition$Reference() {
      super(
         "io.micronaut.discovery.config.DefaultCompositeConfigurationClient",
         "io.micronaut.discovery.config.$DefaultCompositeConfigurationClient$Definition",
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
      return new $DefaultCompositeConfigurationClient$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $DefaultCompositeConfigurationClient$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return DefaultCompositeConfigurationClient.class;
   }
}
