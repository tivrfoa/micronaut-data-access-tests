package io.micronaut.http.server.netty.binders;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.netty.HttpContentProcessorResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

// $FF: synthetic class
@Generated
class $NettyBinderRegistrar$Definition extends AbstractInitializableBeanDefinition<NettyBinderRegistrar> implements BeanFactory<NettyBinderRegistrar> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyBinderRegistrar.class,
      "<init>",
      new Argument[]{
         Argument.of(
            ConversionService.class,
            "conversionService",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            Argument.ofTypeVariable(
               ConversionService.class, "Impl", null, Argument.ofTypeVariable(ConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS)
            )
         ),
         Argument.of(HttpContentProcessorResolver.class, "httpContentProcessorResolver"),
         Argument.of(BeanLocator.class, "beanLocator"),
         Argument.of(BeanProvider.class, "httpServerConfiguration", null, Argument.ofTypeVariable(HttpServerConfiguration.class, "T")),
         Argument.of(
            BeanProvider.class,
            "executorService",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "io")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "io")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("javax.inject.Named")),
               false,
               true
            ),
            Argument.ofTypeVariable(ExecutorService.class, "T")
         )
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.event.BeanCreatedEventListener", new Argument[]{Argument.of(RequestBinderRegistry.class, "T")}
   );

   @Override
   public NettyBinderRegistrar build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NettyBinderRegistrar var4 = new NettyBinderRegistrar(
         (ConversionService<?>)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (HttpContentProcessorResolver)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (BeanLocator)super.getBeanForConstructorArgument(var1, var2, 2, null),
         (BeanProvider<HttpServerConfiguration>)super.getBeanForConstructorArgument(var1, var2, 3, null),
         (BeanProvider<ExecutorService>)super.getBeanForConstructorArgument(var1, var2, 4, Qualifiers.byName("io"))
      );
      return (NettyBinderRegistrar)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      NettyBinderRegistrar var4 = (NettyBinderRegistrar)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $NettyBinderRegistrar$Definition() {
      this(NettyBinderRegistrar.class, $CONSTRUCTOR);
   }

   protected $NettyBinderRegistrar$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyBinderRegistrar$Definition$Reference.$ANNOTATION_METADATA,
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
         false,
         false,
         false,
         false
      );
   }
}
