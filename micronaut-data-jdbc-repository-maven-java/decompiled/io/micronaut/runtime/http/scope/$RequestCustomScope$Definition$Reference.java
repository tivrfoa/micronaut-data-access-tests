package io.micronaut.runtime.http.scope;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $RequestCustomScope$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
      Map var0;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("javax.inject.Singleton", Collections.EMPTY_MAP),
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
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                  ),
                  new AnnotationValue("io.micronaut.core.annotation.Indexed", AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()), var0)
               }
            ),
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
         false,
         true
      );
   }

   public $RequestCustomScope$Definition$Reference() {
      super(
         "io.micronaut.runtime.http.scope.RequestCustomScope",
         "io.micronaut.runtime.http.scope.$RequestCustomScope$Definition",
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
      return new $RequestCustomScope$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $RequestCustomScope$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return RequestCustomScope.class;
   }
}
