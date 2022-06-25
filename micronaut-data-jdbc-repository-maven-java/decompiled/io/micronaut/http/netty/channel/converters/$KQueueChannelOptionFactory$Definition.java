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
class $KQueueChannelOptionFactory$Definition
   extends AbstractInitializableBeanDefinition<KQueueChannelOptionFactory>
   implements BeanFactory<KQueueChannelOptionFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      KQueueChannelOptionFactory.class, "<init>", null, null, false
   );

   @Override
   public KQueueChannelOptionFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      KQueueChannelOptionFactory var4 = new KQueueChannelOptionFactory();
      return (KQueueChannelOptionFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      KQueueChannelOptionFactory var4 = (KQueueChannelOptionFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $KQueueChannelOptionFactory$Definition() {
      this(KQueueChannelOptionFactory.class, $CONSTRUCTOR);
   }

   protected $KQueueChannelOptionFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $KQueueChannelOptionFactory$Definition$Reference.$ANNOTATION_METADATA,
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
