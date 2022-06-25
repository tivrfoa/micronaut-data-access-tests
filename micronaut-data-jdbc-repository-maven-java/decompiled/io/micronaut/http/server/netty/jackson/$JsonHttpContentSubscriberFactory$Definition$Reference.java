package io.micronaut.http.server.netty.jackson;

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
public final class $JsonHttpContentSubscriberFactory$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.core.annotation.Internal",
         Collections.EMPTY_MAP,
         "io.micronaut.http.annotation.Consumes",
         AnnotationUtil.mapOf("value", new String[]{"application/x-json-stream", "application/json"}),
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "io.micronaut.core.annotation.Internal",
         Collections.EMPTY_MAP,
         "io.micronaut.http.annotation.Consumes",
         AnnotationUtil.mapOf("value", new String[]{"application/x-json-stream", "application/json"}),
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
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
   }

   public $JsonHttpContentSubscriberFactory$Definition$Reference() {
      super(
         "io.micronaut.http.server.netty.jackson.JsonHttpContentSubscriberFactory",
         "io.micronaut.http.server.netty.jackson.$JsonHttpContentSubscriberFactory$Definition",
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
      return new $JsonHttpContentSubscriberFactory$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $JsonHttpContentSubscriberFactory$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return JsonHttpContentSubscriberFactory.class;
   }
}
