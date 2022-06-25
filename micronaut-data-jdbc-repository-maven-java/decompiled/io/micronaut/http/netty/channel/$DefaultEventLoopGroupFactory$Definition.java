package io.micronaut.http.netty.channel;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.netty.configuration.NettyGlobalConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultEventLoopGroupFactory$Definition
   extends AbstractInitializableBeanDefinition<DefaultEventLoopGroupFactory>
   implements BeanFactory<DefaultEventLoopGroupFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultEventLoopGroupFactory.class,
      "<init>",
      new Argument[]{
         Argument.of(NioEventLoopGroupFactory.class, "nioEventLoopGroupFactory"),
         Argument.of(
            EventLoopGroupFactory.class,
            "nativeFactory",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP, "javax.inject.Named", AnnotationUtil.mapOf("value", "native")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP, "javax.inject.Named", AnnotationUtil.mapOf("value", "native")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("javax.inject.Named")),
               false,
               true
            ),
            null
         ),
         Argument.of(
            NettyGlobalConfiguration.class,
            "nettyGlobalConfiguration",
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
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );

   @Override
   public DefaultEventLoopGroupFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultEventLoopGroupFactory var4 = new DefaultEventLoopGroupFactory(
         (NioEventLoopGroupFactory)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (EventLoopGroupFactory)super.getBeanForConstructorArgument(var1, var2, 1, Qualifiers.byName("native")),
         (NettyGlobalConfiguration)super.getBeanForConstructorArgument(var1, var2, 2, null)
      );
      return (DefaultEventLoopGroupFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultEventLoopGroupFactory var4 = (DefaultEventLoopGroupFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultEventLoopGroupFactory$Definition() {
      this(DefaultEventLoopGroupFactory.class, $CONSTRUCTOR);
   }

   protected $DefaultEventLoopGroupFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultEventLoopGroupFactory$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
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
