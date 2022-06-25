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
class $NioEventLoopGroupFactory$Definition
   extends AbstractInitializableBeanDefinition<NioEventLoopGroupFactory>
   implements BeanFactory<NioEventLoopGroupFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NioEventLoopGroupFactory.class, "<init>", null, null, false
   );

   @Override
   public NioEventLoopGroupFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NioEventLoopGroupFactory var4 = new NioEventLoopGroupFactory();
      return (NioEventLoopGroupFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      NioEventLoopGroupFactory var4 = (NioEventLoopGroupFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $NioEventLoopGroupFactory$Definition() {
      this(NioEventLoopGroupFactory.class, $CONSTRUCTOR);
   }

   protected $NioEventLoopGroupFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NioEventLoopGroupFactory$Definition$Reference.$ANNOTATION_METADATA,
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
