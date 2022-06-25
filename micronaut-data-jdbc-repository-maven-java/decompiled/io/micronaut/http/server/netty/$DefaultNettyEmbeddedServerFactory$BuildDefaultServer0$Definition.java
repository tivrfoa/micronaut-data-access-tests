package io.micronaut.http.server.netty;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.runtime.server.EmbeddedServer;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultNettyEmbeddedServerFactory$BuildDefaultServer0$Definition
   extends AbstractInitializableBeanDefinition<NettyEmbeddedServer>
   implements BeanFactory<NettyEmbeddedServer> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultNettyEmbeddedServerFactory.class,
      "buildDefaultServer",
      new Argument[]{
         Argument.of(
            NettyHttpServerConfiguration.class,
            "configuration",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            null
         )
      },
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Primary",
            Collections.EMPTY_MAP,
            "javax.annotation.Nonnull",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Primary",
            Collections.EMPTY_MAP,
            "io.micronaut.core.annotation.Indexes",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Indexed",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_0()),
                     AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                  )
               }
            ),
            "javax.annotation.Nonnull",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "javax.inject.Qualifier",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton")
         ),
         false,
         true
      ),
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.ApplicationContextLifeCycle",
      new Argument[]{Argument.of(EmbeddedServer.class, "T")},
      "io.micronaut.context.event.ApplicationEventListener",
      new Argument[]{Argument.of(RefreshEvent.class, "E")},
      "io.micronaut.runtime.EmbeddedApplication",
      new Argument[]{Argument.of(EmbeddedServer.class, "T")}
   );

   @Override
   public NettyEmbeddedServer build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, DefaultNettyEmbeddedServerFactory.class, null);
      var1.markDependentAsFactory();
      NettyEmbeddedServer var5 = ((DefaultNettyEmbeddedServerFactory)var4)
         .buildDefaultServer((NettyHttpServerConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (NettyEmbeddedServer)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      NettyEmbeddedServer var4 = (NettyEmbeddedServer)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultNettyEmbeddedServerFactory$BuildDefaultServer0$Definition() {
      this(NettyEmbeddedServer.class, $CONSTRUCTOR);
   }

   protected $DefaultNettyEmbeddedServerFactory$BuildDefaultServer0$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultNettyEmbeddedServerFactory$BuildDefaultServer0$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         true,
         false,
         false,
         false
      );
   }
}
