package io.micronaut.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $ObjectMapperFactory$JsonFactory0$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      Map var0;
      $ANNOTATION_METADATA = new AnnotationMetadataHierarchy(
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.BootstrapContextCompatible",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Factory",
               Collections.EMPTY_MAP,
               "jakarta.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.BootstrapContextCompatible",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Factory",
               Collections.EMPTY_MAP,
               "jakarta.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
            false,
            true
         ),
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.BootstrapContextCompatible",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_1()}),
                        var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                     )
                  }
               ),
               "javax.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
            AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.BootstrapContextCompatible",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_1()}),
                        var0
                     )
                  }
               ),
               "javax.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
            false,
            true
         )
      );
   }

   public $ObjectMapperFactory$JsonFactory0$Definition$Reference() {
      super(
         "com.fasterxml.jackson.core.JsonFactory",
         "io.micronaut.jackson.$ObjectMapperFactory$JsonFactory0$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         true,
         false,
         true,
         false,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $ObjectMapperFactory$JsonFactory0$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ObjectMapperFactory$JsonFactory0$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return JsonFactory.class;
   }
}
