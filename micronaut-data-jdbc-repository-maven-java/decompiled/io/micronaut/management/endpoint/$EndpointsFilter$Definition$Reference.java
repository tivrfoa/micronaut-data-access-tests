package io.micronaut.management.endpoint;

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
public final class $EndpointsFilter$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf("io.micronaut.http.annotation.Filter", AnnotationUtil.mapOf("value", new String[]{"/**"})),
      AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("io.micronaut.http.annotation.Filter", AnnotationUtil.mapOf("value", new String[]{"/**"})),
      AnnotationUtil.mapOf(
         "javax.inject.Scope",
         AnnotationUtil.internListOf("javax.inject.Singleton"),
         "javax.inject.Singleton",
         AnnotationUtil.internListOf("io.micronaut.http.annotation.Filter")
      ),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(),
         AnnotationUtil.mapOf(
            "methods",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "patternStyle",
            "ANT",
            "patterns",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "serviceId",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "value",
            ArrayUtils.EMPTY_OBJECT_ARRAY
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
   }

   public $EndpointsFilter$Definition$Reference() {
      super(
         "io.micronaut.management.endpoint.EndpointsFilter",
         "io.micronaut.management.endpoint.$EndpointsFilter$Definition",
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
      return new $EndpointsFilter$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $EndpointsFilter$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return EndpointsFilter.class;
   }
}
