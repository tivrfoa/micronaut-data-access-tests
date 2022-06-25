package io.micronaut.http.server.netty;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultHttpContentProcessorResolver$Definition
   extends AbstractInitializableBeanDefinition<DefaultHttpContentProcessorResolver>
   implements BeanFactory<DefaultHttpContentProcessorResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultHttpContentProcessorResolver.class,
      "<init>",
      new Argument[]{
         Argument.of(BeanLocator.class, "beanLocator"),
         Argument.of(BeanProvider.class, "serverConfiguration", null, Argument.ofTypeVariable(NettyHttpServerConfiguration.class, "T"))
      },
      null,
      false
   );

   @Override
   public DefaultHttpContentProcessorResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultHttpContentProcessorResolver var4 = new DefaultHttpContentProcessorResolver(
         (BeanLocator)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (BeanProvider<NettyHttpServerConfiguration>)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (DefaultHttpContentProcessorResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultHttpContentProcessorResolver var4 = (DefaultHttpContentProcessorResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultHttpContentProcessorResolver$Definition() {
      this(DefaultHttpContentProcessorResolver.class, $CONSTRUCTOR);
   }

   protected $DefaultHttpContentProcessorResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultHttpContentProcessorResolver$Definition$Reference.$ANNOTATION_METADATA,
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
