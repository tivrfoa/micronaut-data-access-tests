package io.micronaut.http.netty.channel.converters;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultChannelOptionFactory$Definition
   extends AbstractInitializableBeanDefinition<DefaultChannelOptionFactory>
   implements BeanFactory<DefaultChannelOptionFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultChannelOptionFactory.class, "<init>", null, null, false
   );

   @Override
   public DefaultChannelOptionFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultChannelOptionFactory var4 = new DefaultChannelOptionFactory();
      return (DefaultChannelOptionFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultChannelOptionFactory var4 = (DefaultChannelOptionFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultChannelOptionFactory$Definition() {
      this(DefaultChannelOptionFactory.class, $CONSTRUCTOR);
   }

   protected $DefaultChannelOptionFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultChannelOptionFactory$Definition$Reference.$ANNOTATION_METADATA,
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
