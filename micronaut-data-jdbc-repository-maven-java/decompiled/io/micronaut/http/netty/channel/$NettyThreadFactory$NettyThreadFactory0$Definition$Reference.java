package io.micronaut.http.netty.channel;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.concurrent.ThreadFactory;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $NettyThreadFactory$NettyThreadFactory0$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new AnnotationMetadataHierarchy(
      new DefaultAnnotationMetadata(
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
               new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}
            ),
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.DefaultScope",
            AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
            "javax.inject.Scope",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.DefaultScope",
            AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
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
               new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}
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
      ),
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "javax.inject.Named",
            AnnotationUtil.mapOf("value", "netty"),
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "javax.inject.Named",
            AnnotationUtil.mapOf("value", "netty"),
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "javax.inject.Qualifier",
            AnnotationUtil.internListOf("javax.inject.Named"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton")
         ),
         false,
         true
      )
   );

   public $NettyThreadFactory$NettyThreadFactory0$Definition$Reference() {
      super(
         "java.util.concurrent.ThreadFactory",
         "io.micronaut.http.netty.channel.$NettyThreadFactory$NettyThreadFactory0$Definition",
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
      return new $NettyThreadFactory$NettyThreadFactory0$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $NettyThreadFactory$NettyThreadFactory0$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return ThreadFactory.class;
   }
}
