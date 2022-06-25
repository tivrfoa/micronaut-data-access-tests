package io.micronaut.http.server.netty;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $DefaultNettyEmbeddedServerFactory$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.Bean",
         AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_3(), $micronaut_load_class_value_4()}),
         "io.micronaut.context.annotation.Factory",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.Internal",
         Collections.EMPTY_MAP,
         "jakarta.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_5())),
      AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_5())),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.Bean",
         AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_3(), $micronaut_load_class_value_4()}),
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
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_1(), AnnotationUtil.mapOf("typed", ArrayUtils.EMPTY_OBJECT_ARRAY));
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_2());
   }

   public $DefaultNettyEmbeddedServerFactory$Definition$Reference() {
      super(
         "io.micronaut.http.server.netty.DefaultNettyEmbeddedServerFactory",
         "io.micronaut.http.server.netty.$DefaultNettyEmbeddedServerFactory$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         false,
         false,
         true,
         false,
         true,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $DefaultNettyEmbeddedServerFactory$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $DefaultNettyEmbeddedServerFactory$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return DefaultNettyEmbeddedServerFactory.class;
   }
}
