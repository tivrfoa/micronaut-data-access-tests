package io.micronaut.http.client;

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
public final class $ServiceHttpClientConfiguration$ServiceSslClientConfiguration$DefaultTrustStoreConfiguration$Definition$Reference
   extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationProperties",
         AnnotationUtil.mapOf("value", "trust-store"),
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("prefix", "micronaut.http.services.*.ssl.trust-store")
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("value", "trust-store"),
         "javax.inject.Scope",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("value", "trust-store"),
         "javax.inject.Scope",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationProperties",
         AnnotationUtil.mapOf("value", "trust-store"),
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("prefix", "micronaut.http.services.*.ssl.trust-store")
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

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(),
         AnnotationUtil.mapOf("cliPrefix", ArrayUtils.EMPTY_OBJECT_ARRAY, "excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY)
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_1(), AnnotationUtil.mapOf("excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY)
      );
   }

   public $ServiceHttpClientConfiguration$ServiceSslClientConfiguration$DefaultTrustStoreConfiguration$Definition$Reference() {
      super(
         "io.micronaut.http.client.ServiceHttpClientConfiguration$ServiceSslClientConfiguration$DefaultTrustStoreConfiguration",
         "io.micronaut.http.client.$ServiceHttpClientConfiguration$ServiceSslClientConfiguration$DefaultTrustStoreConfiguration$Definition",
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
      return new $ServiceHttpClientConfiguration$ServiceSslClientConfiguration$DefaultTrustStoreConfiguration$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ServiceHttpClientConfiguration$ServiceSslClientConfiguration$DefaultTrustStoreConfiguration$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultTrustStoreConfiguration.class;
   }
}
