package io.micronaut.http.resource;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.io.ResourceResolver;
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
public final class $ResourceLoaderFactory$ResourceResolver2$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      Map var0;
      $ANNOTATION_METADATA = new AnnotationMetadataHierarchy(
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.BootstrapContextCompatible",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Factory",
               Collections.EMPTY_MAP,
               "jakarta.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
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
         ),
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.BootstrapContextCompatible",
               Collections.EMPTY_MAP,
               "io.micronaut.core.annotation.Indexes",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.core.annotation.Indexed",
                        AnnotationUtil.mapOf("value", $micronaut_load_class_value_1()),
                        var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                     )
                  }
               ),
               "javax.annotation.Nonnull",
               Collections.EMPTY_MAP,
               "javax.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
            AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.BootstrapContextCompatible",
               Collections.EMPTY_MAP,
               "io.micronaut.core.annotation.Indexes",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue("io.micronaut.core.annotation.Indexed", AnnotationUtil.mapOf("value", $micronaut_load_class_value_1()), var0)
                  }
               ),
               "javax.annotation.Nonnull",
               Collections.EMPTY_MAP,
               "javax.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
            false,
            true
         )
      );
   }

   public $ResourceLoaderFactory$ResourceResolver2$Definition$Reference() {
      super(
         "io.micronaut.core.io.ResourceResolver",
         "io.micronaut.http.resource.$ResourceLoaderFactory$ResourceResolver2$Definition",
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
      return new $ResourceLoaderFactory$ResourceResolver2$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ResourceLoaderFactory$ResourceResolver2$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return ResourceResolver.class;
   }
}
