package io.micronaut.http.client.interceptor.configuration;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
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
public final class $NamedClientVersioningConfiguration$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("prefix", "micronaut.http.client.versioning.*"),
         "io.micronaut.context.annotation.EachProperty",
         AnnotationUtil.mapOf("primary", "default", "value", "micronaut.http.client.versioning")
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("value", "micronaut.http.client.versioning"),
         "javax.inject.Scope",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("value", "micronaut.http.client.versioning"),
         "javax.inject.Scope",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("prefix", "micronaut.http.client.versioning.*"),
         "io.micronaut.context.annotation.EachProperty",
         AnnotationUtil.mapOf("primary", "default", "value", "micronaut.http.client.versioning")
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.EachProperty"),
         "javax.inject.Scope",
         AnnotationUtil.internListOf("javax.inject.Singleton"),
         "javax.inject.Singleton",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.EachProperty")
      ),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(),
         AnnotationUtil.mapOf("excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY, "list", false)
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_1(), AnnotationUtil.mapOf("excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY)
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_2());
   }

   public $NamedClientVersioningConfiguration$Definition$Reference() {
      super(
         "io.micronaut.http.client.interceptor.configuration.NamedClientVersioningConfiguration",
         "io.micronaut.http.client.interceptor.configuration.$NamedClientVersioningConfiguration$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         false,
         false,
         true,
         true,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $NamedClientVersioningConfiguration$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $NamedClientVersioningConfiguration$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return NamedClientVersioningConfiguration.class;
   }
}
