package io.micronaut.http.server.netty;

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
public final class $FormDataHttpContentSubscriberFactory$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.http.annotation.Consumes",
         AnnotationUtil.mapOf("value", new String[]{"application/x-www-form-urlencoded", "multipart/form-data"}),
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "io.micronaut.http.annotation.Consumes",
         AnnotationUtil.mapOf("value", new String[]{"application/x-www-form-urlencoded", "multipart/form-data"}),
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(), AnnotationUtil.mapOf("single", false, "value", new String[]{"application/json"})
      );
   }

   public $FormDataHttpContentSubscriberFactory$Definition$Reference() {
      super(
         "io.micronaut.http.server.netty.FormDataHttpContentSubscriberFactory",
         "io.micronaut.http.server.netty.$FormDataHttpContentSubscriberFactory$Definition",
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
      return new $FormDataHttpContentSubscriberFactory$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $FormDataHttpContentSubscriberFactory$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return FormDataHttpContentSubscriberFactory.class;
   }
}
