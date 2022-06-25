package io.micronaut.runtime.http.codec;

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
public final class $MediaTypeCodecRegistryFactory$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.BootstrapContextCompatible",
         Collections.EMPTY_MAP,
         "io.micronaut.context.annotation.Factory",
         Collections.EMPTY_MAP,
         "jakarta.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2())),
      AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2())),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.BootstrapContextCompatible",
         Collections.EMPTY_MAP,
         "io.micronaut.context.annotation.Factory",
         Collections.EMPTY_MAP,
         "jakarta.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
   }

   public $MediaTypeCodecRegistryFactory$Definition$Reference() {
      super(
         "io.micronaut.runtime.http.codec.MediaTypeCodecRegistryFactory",
         "io.micronaut.runtime.http.codec.$MediaTypeCodecRegistryFactory$Definition",
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
      return new $MediaTypeCodecRegistryFactory$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $MediaTypeCodecRegistryFactory$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return MediaTypeCodecRegistryFactory.class;
   }
}
