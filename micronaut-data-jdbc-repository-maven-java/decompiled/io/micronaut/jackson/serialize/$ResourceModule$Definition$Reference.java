package io.micronaut.jackson.serialize;

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
public final class $ResourceModule$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.internMapOf("javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
      false,
      true
   );

   public $ResourceModule$Definition$Reference() {
      super(
         "io.micronaut.jackson.serialize.ResourceModule",
         "io.micronaut.jackson.serialize.$ResourceModule$Definition",
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
      return new $ResourceModule$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ResourceModule$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return ResourceModule.class;
   }
}
