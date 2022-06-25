package io.micronaut.http.ssl;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $ClientSslConfiguration$DefaultKeyStoreConfiguration$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(),
         AnnotationUtil.mapOf("cliPrefix", ArrayUtils.EMPTY_OBJECT_ARRAY, "excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY)
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_1(), AnnotationUtil.mapOf("excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY)
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_2(),
         AnnotationUtil.mapOf(
            "beans",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "classes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "condition",
            $micronaut_load_class_value_3(),
            "entities",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "env",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "missing",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "missingBeans",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "missingClasses",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "missingConfigurations",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "notEnv",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "notOs",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "os",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "resources",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "sdk",
            "MICRONAUT"
         )
      );
      Map var0;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.ConfigurationProperties",
            AnnotationUtil.mapOf("value", "key-store"),
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.mapOf("prefix", "micronaut.http.client.ssl.key-store"),
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("property", "micronaut.http.client.ssl.key-store"),
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                  )
               }
            )
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.mapOf("value", "key-store"),
            "javax.inject.Scope",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.mapOf("value", "key-store"),
            "javax.inject.Scope",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.ConfigurationProperties",
            AnnotationUtil.mapOf("value", "key-store"),
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.mapOf("prefix", "micronaut.http.client.ssl.key-store"),
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "micronaut.http.client.ssl.key-store"), var0)
               }
            )
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton"),
            "javax.inject.Singleton",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties")
         ),
         false,
         true
      );
   }

   public $ClientSslConfiguration$DefaultKeyStoreConfiguration$Definition$Reference() {
      super(
         "io.micronaut.http.ssl.ClientSslConfiguration$DefaultKeyStoreConfiguration",
         "io.micronaut.http.ssl.$ClientSslConfiguration$DefaultKeyStoreConfiguration$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         true,
         false,
         true,
         true,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $ClientSslConfiguration$DefaultKeyStoreConfiguration$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ClientSslConfiguration$DefaultKeyStoreConfiguration$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return ClientSslConfiguration.DefaultKeyStoreConfiguration.class;
   }
}
