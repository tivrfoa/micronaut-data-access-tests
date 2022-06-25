package io.micronaut.http.hateoas;

import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.graal.GraalReflectionConfigurer;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated(
   service = "io/micronaut/core/graal/GraalReflectionConfigurer"
)
public final class $AbstractResource$ReflectConfig implements GraalReflectionConfigurer {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      Map var0;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.mapOf(
            "io.micronaut.core.annotation.ReflectionConfig$ReflectionConfigList",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.ReflectionConfig",
                     AnnotationUtil.mapOf(
                        "accessType",
                        new TypeHint.AccessType[0],
                        "methods",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.core.annotation.ReflectionConfig$ReflectiveMethodConfig",
                              AnnotationUtil.mapOf("name", "setLinks", "parameterTypes", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.ReflectionConfig$ReflectiveMethodConfig")
                           ),
                           new AnnotationValue(
                              "io.micronaut.core.annotation.ReflectionConfig$ReflectiveMethodConfig",
                              AnnotationUtil.mapOf("name", "setEmbedded", "parameterTypes", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                              var0
                           )
                        },
                        "type",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.ReflectionConfig")
                  )
               }
            )
         ),
         Collections.EMPTY_MAP,
         false,
         true
      );
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return $ANNOTATION_METADATA;
   }
}
