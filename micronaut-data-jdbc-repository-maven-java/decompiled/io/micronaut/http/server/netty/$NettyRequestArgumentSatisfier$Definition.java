package io.micronaut.http.server.netty;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $NettyRequestArgumentSatisfier$Definition
   extends AbstractInitializableBeanDefinition<NettyRequestArgumentSatisfier>
   implements BeanFactory<NettyRequestArgumentSatisfier> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyRequestArgumentSatisfier.class, "<init>", new Argument[]{Argument.of(RequestBinderRegistry.class, "requestBinderRegistry")}, null, false
   );

   @Override
   public NettyRequestArgumentSatisfier build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NettyRequestArgumentSatisfier var4 = new NettyRequestArgumentSatisfier((RequestBinderRegistry)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (NettyRequestArgumentSatisfier)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      NettyRequestArgumentSatisfier var4 = (NettyRequestArgumentSatisfier)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $NettyRequestArgumentSatisfier$Definition() {
      this(NettyRequestArgumentSatisfier.class, $CONSTRUCTOR);
   }

   protected $NettyRequestArgumentSatisfier$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyRequestArgumentSatisfier$Definition$Reference.$ANNOTATION_METADATA,
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
