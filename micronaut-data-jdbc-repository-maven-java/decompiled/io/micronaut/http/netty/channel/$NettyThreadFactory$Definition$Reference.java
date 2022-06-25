package io.micronaut.http.netty.channel;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
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
public final class $NettyThreadFactory$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.BootstrapContextCompatible",
         Collections.EMPTY_MAP,
         "io.micronaut.context.annotation.Factory",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.TypeHint",
         AnnotationUtil.mapOf(
            "accessType",
            new String[]{"ALL_DECLARED_CONSTRUCTORS", "ALL_DECLARED_FIELDS", "ALL_PUBLIC_CONSTRUCTORS"},
            "typeNames",
            new String[]{"sun.security.ssl.SSLContextImpl$TLSContext"},
            "value",
            new AnnotationClassValue[]{$micronaut_load_class_value_3(), $micronaut_load_class_value_4()}
         ),
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.DefaultScope",
         AnnotationUtil.mapOf("value", $micronaut_load_class_value_5()),
         "javax.inject.Scope",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.DefaultScope",
         AnnotationUtil.mapOf("value", $micronaut_load_class_value_5()),
         "javax.inject.Scope",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.BootstrapContextCompatible",
         Collections.EMPTY_MAP,
         "io.micronaut.context.annotation.Factory",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.TypeHint",
         AnnotationUtil.mapOf(
            "accessType",
            new String[]{"ALL_DECLARED_CONSTRUCTORS", "ALL_DECLARED_FIELDS", "ALL_PUBLIC_CONSTRUCTORS"},
            "typeNames",
            new String[]{"sun.security.ssl.SSLContextImpl$TLSContext"},
            "value",
            new AnnotationClassValue[]{$micronaut_load_class_value_3(), $micronaut_load_class_value_4()}
         ),
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.DefaultScope",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory"),
         "javax.inject.Scope",
         AnnotationUtil.internListOf("javax.inject.Singleton")
      ),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(),
         AnnotationUtil.mapOf(
            "accessType", new String[]{"ALL_DECLARED_CONSTRUCTORS"}, "typeNames", ArrayUtils.EMPTY_OBJECT_ARRAY, "value", ArrayUtils.EMPTY_OBJECT_ARRAY
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_2());
   }

   public $NettyThreadFactory$Definition$Reference() {
      super(
         "io.micronaut.http.netty.channel.NettyThreadFactory",
         "io.micronaut.http.netty.channel.$NettyThreadFactory$Definition",
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
      return new $NettyThreadFactory$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $NettyThreadFactory$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return NettyThreadFactory.class;
   }
}
