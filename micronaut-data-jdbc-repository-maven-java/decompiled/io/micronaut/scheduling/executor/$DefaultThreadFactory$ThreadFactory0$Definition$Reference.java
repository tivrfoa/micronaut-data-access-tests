package io.micronaut.scheduling.executor;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.concurrent.ThreadFactory;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $DefaultThreadFactory$ThreadFactory0$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new AnnotationMetadataHierarchy(
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf("io.micronaut.context.annotation.Factory", Collections.EMPTY_MAP, "jakarta.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.Factory", Collections.EMPTY_MAP, "jakarta.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
         false,
         true
      ),
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf("io.micronaut.context.annotation.Primary", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.Primary", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
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

   public $DefaultThreadFactory$ThreadFactory0$Definition$Reference() {
      super(
         "java.util.concurrent.ThreadFactory",
         "io.micronaut.scheduling.executor.$DefaultThreadFactory$ThreadFactory0$Definition",
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
      return new $DefaultThreadFactory$ThreadFactory0$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $DefaultThreadFactory$ThreadFactory0$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return ThreadFactory.class;
   }
}
