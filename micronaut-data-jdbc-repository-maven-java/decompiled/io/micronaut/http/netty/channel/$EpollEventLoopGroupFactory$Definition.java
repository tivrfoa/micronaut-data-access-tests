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
class $EpollEventLoopGroupFactory$Definition
   extends AbstractInitializableBeanDefinition<EpollEventLoopGroupFactory>
   implements BeanFactory<EpollEventLoopGroupFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      EpollEventLoopGroupFactory.class, "<init>", null, null, false
   );

   @Override
   public EpollEventLoopGroupFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      EpollEventLoopGroupFactory var4 = new EpollEventLoopGroupFactory();
      return (EpollEventLoopGroupFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      EpollEventLoopGroupFactory var4 = (EpollEventLoopGroupFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $EpollEventLoopGroupFactory$Definition() {
      this(EpollEventLoopGroupFactory.class, $CONSTRUCTOR);
   }

   protected $EpollEventLoopGroupFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $EpollEventLoopGroupFactory$Definition$Reference.$ANNOTATION_METADATA,
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
