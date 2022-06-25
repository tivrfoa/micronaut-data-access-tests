package io.micronaut.http.client.netty;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $DefaultNettyHttpClientRegistry$HttpClient0$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new AnnotationMetadataHierarchy(
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
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
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
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
            "io.micronaut.context.annotation.Bean",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Primary",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
         AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Bean",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Primary",
            Collections.EMPTY_MAP,
            "io.micronaut.core.annotation.Internal",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary")),
         false,
         true
      )
   );

   public $DefaultNettyHttpClientRegistry$HttpClient0$Definition$Reference() {
      super(
         "io.micronaut.http.client.netty.DefaultHttpClient",
         "io.micronaut.http.client.netty.$DefaultNettyHttpClientRegistry$HttpClient0$Definition",
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
      return new $DefaultNettyHttpClientRegistry$HttpClient0$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $DefaultNettyHttpClientRegistry$HttpClient0$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return DefaultHttpClient.class;
   }
}
