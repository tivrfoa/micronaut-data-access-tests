package io.micronaut.http.netty.channel;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

// $FF: synthetic class
@Generated
class $NettyThreadFactory$NettyThreadFactory0$Definition extends AbstractInitializableBeanDefinition<ThreadFactory> implements BeanFactory<ThreadFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyThreadFactory.class,
      "nettyThreadFactory",
      null,
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
      ),
      false
   );

   @Override
   public ThreadFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, NettyThreadFactory.class, null);
      var1.markDependentAsFactory();
      ThreadFactory var5 = ((NettyThreadFactory)var4).nettyThreadFactory();
      return (ThreadFactory)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ThreadFactory var4 = (ThreadFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $NettyThreadFactory$NettyThreadFactory0$Definition() {
      this(ThreadFactory.class, $CONSTRUCTOR);
   }

   protected $NettyThreadFactory$NettyThreadFactory0$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyThreadFactory$NettyThreadFactory0$Definition$Reference.$ANNOTATION_METADATA,
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
         false,
         false,
         false,
         false
      );
   }
}
