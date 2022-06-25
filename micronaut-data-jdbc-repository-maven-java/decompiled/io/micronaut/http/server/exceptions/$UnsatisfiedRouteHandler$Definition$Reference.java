package io.micronaut.http.server.exceptions;

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
public final class $UnsatisfiedRouteHandler$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf("io.micronaut.http.annotation.Produces", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("io.micronaut.http.annotation.Produces", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(), AnnotationUtil.mapOf("single", false, "value", new String[]{"application/json"})
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_1(), AnnotationUtil.mapOf("processOnStartup", false));
   }

   public $UnsatisfiedRouteHandler$Definition$Reference() {
      super(
         "io.micronaut.http.server.exceptions.UnsatisfiedRouteHandler",
         "io.micronaut.http.server.exceptions.$UnsatisfiedRouteHandler$Definition",
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
      return new $UnsatisfiedRouteHandler$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $UnsatisfiedRouteHandler$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return UnsatisfiedRouteHandler.class;
   }
}
