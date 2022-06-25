package io.micronaut.jackson.serialize;

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
public final class $JacksonObjectSerializer$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.Bean",
         AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_1()}),
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.Bean",
         AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_1()}),
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_0(), AnnotationUtil.mapOf("typed", ArrayUtils.EMPTY_OBJECT_ARRAY));
   }

   public $JacksonObjectSerializer$Definition$Reference() {
      super(
         "io.micronaut.jackson.serialize.JacksonObjectSerializer",
         "io.micronaut.jackson.serialize.$JacksonObjectSerializer$Definition",
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
      return new $JacksonObjectSerializer$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $JacksonObjectSerializer$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return JacksonObjectSerializer.class;
   }
}
