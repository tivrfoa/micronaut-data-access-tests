package io.micronaut.http.server.netty;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $DefaultNettyEmbeddedServerFactory$BuildDefaultServer0$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new AnnotationMetadataHierarchy(
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Bean",
            AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}),
            "io.micronaut.context.annotation.Factory",
            Collections.EMPTY_MAP,
            "io.micronaut.core.annotation.Internal",
            Collections.EMPTY_MAP,
            "jakarta.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2())),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2())),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Bean",
            AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}),
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
            "io.micronaut.context.annotation.Primary",
            Collections.EMPTY_MAP,
            "javax.annotation.Nonnull",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
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
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
                     AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                  )
               }
            ),
            "javax.annotation.Nonnull",
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
      )
   );

   public $DefaultNettyEmbeddedServerFactory$BuildDefaultServer0$Definition$Reference() {
      super(
         "io.micronaut.http.server.netty.NettyEmbeddedServer",
         "io.micronaut.http.server.netty.$DefaultNettyEmbeddedServerFactory$BuildDefaultServer0$Definition",
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
      return new $DefaultNettyEmbeddedServerFactory$BuildDefaultServer0$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $DefaultNettyEmbeddedServerFactory$BuildDefaultServer0$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return NettyEmbeddedServer.class;
   }
}
