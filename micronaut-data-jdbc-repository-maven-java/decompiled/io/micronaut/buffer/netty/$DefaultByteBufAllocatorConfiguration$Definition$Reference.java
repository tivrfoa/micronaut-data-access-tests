package io.micronaut.buffer.netty;

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
public final class $DefaultByteBufAllocatorConfiguration$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
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
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_4());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_5());
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_6(), AnnotationUtil.mapOf("value", 0));
      Map var0;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.ConfigurationProperties",
            AnnotationUtil.mapOf("value", "netty.default.allocator"),
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.mapOf("prefix", "netty.default.allocator"),
            "io.micronaut.context.annotation.Context",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("property", "netty.default.allocator"),
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                  )
               }
            ),
            "io.micronaut.core.annotation.Internal",
            Collections.EMPTY_MAP,
            "io.micronaut.core.annotation.Order",
            AnnotationUtil.mapOf("value", Integer.MIN_VALUE)
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.mapOf("value", "netty.default.allocator"),
            "javax.inject.Scope",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.mapOf("value", "netty.default.allocator"),
            "javax.inject.Scope",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.ConfigurationProperties",
            AnnotationUtil.mapOf("value", "netty.default.allocator"),
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.mapOf("prefix", "netty.default.allocator"),
            "io.micronaut.context.annotation.Context",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "netty.default.allocator"), var0)
               }
            ),
            "io.micronaut.core.annotation.Internal",
            Collections.EMPTY_MAP,
            "io.micronaut.core.annotation.Order",
            AnnotationUtil.mapOf("value", Integer.MIN_VALUE)
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton"),
            "javax.inject.Singleton",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties", "io.micronaut.context.annotation.Context")
         ),
         false,
         true
      );
   }

   public $DefaultByteBufAllocatorConfiguration$Definition$Reference() {
      super(
         "io.micronaut.buffer.netty.DefaultByteBufAllocatorConfiguration",
         "io.micronaut.buffer.netty.$DefaultByteBufAllocatorConfiguration$Definition",
         $ANNOTATION_METADATA,
         false,
         true,
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
      return new $DefaultByteBufAllocatorConfiguration$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $DefaultByteBufAllocatorConfiguration$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return DefaultByteBufAllocatorConfiguration.class;
   }
}
