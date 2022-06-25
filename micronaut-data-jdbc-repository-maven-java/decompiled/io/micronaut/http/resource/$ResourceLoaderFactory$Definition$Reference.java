package io.micronaut.http.resource;

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
public final class $ResourceLoaderFactory$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
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

   public $ResourceLoaderFactory$Definition$Reference() {
      super(
         "io.micronaut.http.resource.ResourceLoaderFactory",
         "io.micronaut.http.resource.$ResourceLoaderFactory$Definition",
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
      return new $ResourceLoaderFactory$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ResourceLoaderFactory$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return ResourceLoaderFactory.class;
   }
}
