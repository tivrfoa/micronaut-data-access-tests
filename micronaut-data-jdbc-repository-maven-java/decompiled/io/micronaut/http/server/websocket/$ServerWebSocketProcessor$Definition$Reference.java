package io.micronaut.http.server.websocket;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $ServerWebSocketProcessor$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf("io.micronaut.core.annotation.Internal", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "io.micronaut.core.annotation.Indexes",
         AnnotationUtil.mapOf(
            "value",
            new AnnotationValue[]{
               new AnnotationValue(
                  "io.micronaut.core.annotation.Indexed",
                  AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
                  AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
               )
            }
         ),
         "io.micronaut.core.annotation.Internal",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
   }

   public $ServerWebSocketProcessor$Definition$Reference() {
      super(
         "io.micronaut.http.server.websocket.ServerWebSocketProcessor",
         "io.micronaut.http.server.websocket.$ServerWebSocketProcessor$Definition",
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
      return new $ServerWebSocketProcessor$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ServerWebSocketProcessor$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return ServerWebSocketProcessor.class;
   }
}
