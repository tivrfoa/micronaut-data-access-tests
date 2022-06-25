package io.micronaut.http.server.netty;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.netty.channel.EventLoopGroupFactory;
import io.micronaut.http.netty.channel.EventLoopGroupRegistry;
import io.micronaut.http.netty.channel.converters.ChannelOptionFactory;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.http.server.netty.ssl.ServerSslBuilder;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.web.router.resource.StaticResourceResolver;
import io.netty.channel.ChannelOutboundHandler;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

// $FF: synthetic class
@Generated
class $DefaultNettyEmbeddedServerFactory$Definition
   extends AbstractInitializableBeanDefinition<DefaultNettyEmbeddedServerFactory>
   implements BeanFactory<DefaultNettyEmbeddedServerFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultNettyEmbeddedServerFactory.class,
      "<init>",
      new Argument[]{
         Argument.of(ApplicationContext.class, "applicationContext"),
         Argument.of(RouteExecutor.class, "routeExecutor"),
         Argument.of(MediaTypeCodecRegistry.class, "mediaTypeCodecRegistry"),
         Argument.of(StaticResourceResolver.class, "staticResourceResolver"),
         Argument.of(
            ThreadFactory.class,
            "nettyThreadFactory",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "netty")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "netty")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("javax.inject.Named")),
               false,
               true
            ),
            null
         ),
         Argument.of(HttpCompressionStrategy.class, "httpCompressionStrategy"),
         Argument.of(EventLoopGroupFactory.class, "eventLoopGroupFactory"),
         Argument.of(EventLoopGroupRegistry.class, "eventLoopGroupRegistry")
      },
      null,
      false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
      new AbstractInitializableBeanDefinition.MethodReference(
         DefaultNettyEmbeddedServerFactory.class,
         "setChannelOptionFactory",
         new Argument[]{
            Argument.of(
               ChannelOptionFactory.class,
               "channelOptionFactory",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            )
         },
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Bean",
                  AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}),
                  "io.micronaut.context.annotation.Factory",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.Internal",
                  Collections.EMPTY_MAP,
                  "jakarta.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2())),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2())),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Bean",
                  AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}),
                  "io.micronaut.context.annotation.Factory",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.Internal",
                  Collections.EMPTY_MAP,
                  "jakarta.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false
      ),
      new AbstractInitializableBeanDefinition.MethodReference(
         DefaultNettyEmbeddedServerFactory.class,
         "setServerSslBuilder",
         new Argument[]{
            Argument.of(
               ServerSslBuilder.class,
               "serverSslBuilder",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            )
         },
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Bean",
                  AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}),
                  "io.micronaut.context.annotation.Factory",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.Internal",
                  Collections.EMPTY_MAP,
                  "jakarta.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2())),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2())),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Bean",
                  AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}),
                  "io.micronaut.context.annotation.Factory",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.Internal",
                  Collections.EMPTY_MAP,
                  "jakarta.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false
      ),
      new AbstractInitializableBeanDefinition.MethodReference(
         DefaultNettyEmbeddedServerFactory.class,
         "setOutboundHandlers",
         new Argument[]{Argument.of(List.class, "outboundHandlers", null, Argument.ofTypeVariable(ChannelOutboundHandler.class, "E"))},
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Bean",
                  AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}),
                  "io.micronaut.context.annotation.Factory",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.Internal",
                  Collections.EMPTY_MAP,
                  "jakarta.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2())),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2())),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Bean",
                  AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}),
                  "io.micronaut.context.annotation.Factory",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.Internal",
                  Collections.EMPTY_MAP,
                  "jakarta.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false
      )
   };
   private static final Set $EXPOSED_TYPES = new HashSet(Arrays.asList(NettyEmbeddedServerFactory.class, DefaultNettyEmbeddedServerFactory.class));

   @Override
   public DefaultNettyEmbeddedServerFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultNettyEmbeddedServerFactory var4 = new DefaultNettyEmbeddedServerFactory(
         var2,
         (RouteExecutor)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (MediaTypeCodecRegistry)super.getBeanForConstructorArgument(var1, var2, 2, null),
         (StaticResourceResolver)super.getBeanForConstructorArgument(var1, var2, 3, null),
         (ThreadFactory)super.getBeanForConstructorArgument(var1, var2, 4, Qualifiers.byName("netty")),
         (HttpCompressionStrategy)super.getBeanForConstructorArgument(var1, var2, 5, null),
         (EventLoopGroupFactory)super.getBeanForConstructorArgument(var1, var2, 6, null),
         (EventLoopGroupRegistry)super.getBeanForConstructorArgument(var1, var2, 7, null)
      );
      return (DefaultNettyEmbeddedServerFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultNettyEmbeddedServerFactory var4 = (DefaultNettyEmbeddedServerFactory)var3;
      var4.setChannelOptionFactory(super.getBeanForMethodArgument(var1, var2, 0, 0, null));
      var4.setServerSslBuilder(super.getBeanForMethodArgument(var1, var2, 1, 0, null));
      var4.setOutboundHandlers(super.getBeansOfTypeForMethodArgument(var1, var2, 2, 0, $INJECTION_METHODS[2].arguments[0].getTypeParameters()[0], null));
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultNettyEmbeddedServerFactory$Definition() {
      this(DefaultNettyEmbeddedServerFactory.class, $CONSTRUCTOR);
   }

   protected $DefaultNettyEmbeddedServerFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultNettyEmbeddedServerFactory$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
         null,
         Optional.empty(),
         false,
         false,
         false,
         true,
         false,
         false,
         false,
         false
      );
   }

   @Override
   public Set getExposedTypes() {
      return $EXPOSED_TYPES;
   }
}
