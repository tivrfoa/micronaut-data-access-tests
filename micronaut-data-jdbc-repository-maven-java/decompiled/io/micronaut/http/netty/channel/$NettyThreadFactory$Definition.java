package io.micronaut.http.netty.channel;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $NettyThreadFactory$Definition extends AbstractInitializableBeanDefinition<NettyThreadFactory> implements BeanFactory<NettyThreadFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyThreadFactory.class, "<init>", null, null, false
   );

   @Override
   public NettyThreadFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NettyThreadFactory var4 = new NettyThreadFactory();
      return (NettyThreadFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      NettyThreadFactory var4 = (NettyThreadFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $NettyThreadFactory$Definition() {
      this(NettyThreadFactory.class, $CONSTRUCTOR);
   }

   protected $NettyThreadFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyThreadFactory$Definition$Reference.$ANNOTATION_METADATA,
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
